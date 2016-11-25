var gulp = require('gulp'),
    tinylr = require('tiny-lr'), // Mini webserver for livereload
    minifyCSS = require('gulp-minify-css'), // CSS minifying
    watch = require('gulp-watch'),
    shell = require('gulp-shell'),
    print = require('gulp-print'),
    jshint = require('gulp-jshint'),
    stylish = require('jshint-stylish'),
    //imagemin = require('gulp-imagemin'), // Img minifying
    uglify = require('gulp-uglify'),    // JS minifying
    concat = require('gulp-concat'),    // Files merging
    rev = require('gulp-rev'),          // Files versioning
    compass = require('gulp-compass'),  // Compass to work with CSS
    rjs = require("gulp-requirejs"),    //A gulp interface to r.js
    connect = require('gulp-connect'),  // Webserver
    rename = require("gulp-rename"),    //Rename files
    useref = require('gulp-useref'),    //Removes <!-- build:js ... blocks from html
    replaceRevRef = require('gulp-rev-manifest-replace'), //Plugin to replace assets urls based on generated manifest file
    reverse = require('reversible'),
    rimraf = require('gulp-rimraf');    // Remove files and folders depricated
    var del = require('del'); //TODO replace gulp-rimraf
    //wait = require('gulp-wait'),
    //
    //server = lr(),
    //livereload = require('gulp-livereload'), // Livereload for Gulp

var buildConfig = {
        jekyllStageConfig : "_config.stage.yml", //saas-stage
        jekyllProdConfig : "_config.prod.yml", //saas-prod
        jekyllGHConfig : "_config.gh.yml", //github-pages
        jekyllEEConfig : "_config.enterprise.yml", //onprem-ccis
        jekyllOnpremSEConfig : "_config.onprem-se.yml" //onprem-se
    };

var paths = {
        src: 'app/',
         // assembly folders
        prod: './target/prod/',
        stage: './target/stage/',
        enterprise: './target/enterprise/',
        onpremSE: './target/onprem-se/',
        gh: './target/gh/',
        // ------------------
        temp: 'target/temp/',
        dist: 'target/dist/', //dist folder
        config: 'assembly/build/' //jekill configurations
};

// This task creates local server
gulp.task('connect', ['gh'], function() {
  connect.server({
    root: paths.gh
  });
});

// --------------------------- Building Prod -----------------------------
//----------------
//----------
gulp.task('prod',['copy_src','duplicate_html','lint','prod_cfg','css','jekyll','rjs','myrev','replace','rmbuild','copy_prod'], function(){

})

gulp.task('prod_cfg', function(){
  return gulp.src(paths.config + buildConfig.jekyllProdConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
})

gulp.task('css', ['copy_src','jekyll'], function() {
  return gulp.src(paths.temp+'site/styles/*.css')
  .pipe(minifyCSS())
  .pipe(gulp.dest(paths.prod + 'site/styles/'));
});

gulp.task('lint',function(){
  gulp.src(['!'+paths.src+'site/scripts/vendor/*.*',paths.src+'site/scripts/**/*.js'])
    .pipe(jshint())
  .pipe(jshint.reporter('jshint-stylish'))
  .pipe(jshint.reporter('fail'))
});

// Builds projects using require.js's optimizer + Minify files with UglifyJS
gulp.task('rjs',['copy_src','jekyll'], function(){
      return  rjs({
            mainConfigFile: paths.temp +'site/scripts/main.js',
            //optimize: 'none', //hardcoded in requirejs plugin
            baseUrl: paths.temp + 'site/scripts',
            wrap: true,
            name: 'main',
            mainFile: paths.temp+'site/index.html',
            out: 'amd-main.js'
        })
      .pipe(print(function(filepath) {
        return "rjs -> built: " + filepath;
      }))
      .pipe(gulp.src(paths.temp +'site/scripts/vendor/require.js'))
      .pipe(reverse({objectMode: true})) // requirejs should be at the begining
      .pipe(concat('amd-app.js'))
      .pipe(uglify())
      .pipe(gulp.dest(paths.prod + 'site/scripts'));
 });

gulp.task('jekyll',['copy_src','prod_cfg'], function () {
       console.log('Jekyll building ......... ');
   return gulp.src(paths.temp+'_config.yml', {read: false})
    .pipe(shell([
      'jekyll build'
    ], {
      cwd: 'target/temp',
      templateData: {
        f: function (s) {
          return s.replace(/$/, '.bak')
        }
      }
    }))
});

// Replaces references with reved names
  gulp.task ('myrev', ['copy_src','prod_cfg','css','rjs','jekyll'], function(){
    return gulp.src([paths.prod + 'site/scripts/amd-app.js', paths.prod + 'site/styles/*.css'],{base:paths.prod})
    //.pipe(wait(1500))
    .pipe(rev())
    .pipe(print(function(filepath) {
        return "rev -> built: " + filepath;
      }))
    .pipe(gulp.dest(paths.prod))
    .pipe(rev.manifest())
    .pipe(print(function(filepath) {
        return "manifest -> built: " + filepath;
      }))
    .pipe(gulp.dest(paths.prod));
  });

// Start replacing rev references
gulp.task('replace',['copy_src','prod_cfg','css','rjs','jekyll','myrev'], function(){

  var manifest = require(paths.prod+'rev-manifest.json')
  console.log('Process rev-manifest.json: \n');
  console.log(manifest);
  return gulp.src([paths.prod+'**/*.html'])
  .pipe(replaceRevRef({
    base: process.cwd()+'/target/prod',
    manifest: manifest,
    path: ''/*,
    cdnPrefix: 'http://absolute.path/cdn/'*/
  }))
  .pipe(gulp.dest(paths.prod));

});

// Removes <!-- build:js ... blocks from html for prod configeration
gulp.task('rmbuild', ['copy_src','prod_cfg','css','rjs','jekyll','myrev','replace'], function(){
  return gulp.src(paths.prod+'**/*.html')
  .pipe(useref())
  .pipe(print(function(filepath) {
        return "remove <!-- build:js ... blocks -> built: " + filepath;
      }))
  .pipe(gulp.dest(paths.prod));

});

gulp.task('copy_prod',['copy_src','duplicate_html','prod_cfg','css','rjs','jekyll','myrev','replace','rmbuild'], function(){
  gulp.src([paths.prod+'/**/*.html', // all HTML
    '!'+paths.prod+'site/custom_pages/**/*.html',
    '!'+paths.prod+'site/admin.html',
    '!'+paths.prod+'site/email-templates_onpremises/*.html',
    paths.prod+'**/amd-app*.js', // minified JS
    paths.prod+'**/*.css', // minified CSS
    paths.prod+'**/*.jpg',
    paths.prod+'**/*.ico',
    paths.prod+'**/*.png',
    paths.prod+'**/*.svg',
    paths.prod+'**/*.txt',  // robots.txt
    paths.prod+'**/modernizr.custom.*.js']
    )
      .pipe(print(function(filepath) {
        if (filepath){return "copy prod -> built: " + filepath;}
        
      }))
  .pipe(gulp.dest(paths.dist+'prod'));
});

// Cleans gulp's folders
gulp.task('clean',function(){
  return gulp.src([paths.temp,paths.prod,paths.stage,paths.dist,paths.enterprise],{ read: false }) // much faster
    .pipe(print(function(filepath) {
      return "Delete: " + filepath;
    }))
    .pipe(rimraf());
})

// --------------------------- Building Stage -----------------------------
//----------------
//----------
gulp.task('stage',['copy_src','stage_cfg','css_stage','jekyll_stage','copy_stage'], function(){

})
// Copies src to temp folder
gulp.task('copy_src', ['duplicate_html','duplicate_error_html'], function(){
  return gulp.src(paths.src + '**/*.*')
  .pipe(gulp.dest(paths.temp))
})

// Duplicate login as index.html to temp folder
gulp.task('duplicate_html', function(){
  return gulp.src(paths.src + '/site/login.html')
  .pipe(rename('index.html'))
  .pipe(gulp.dest(paths.temp))
  .pipe(rename('create-account.html'))
  .pipe(gulp.dest(paths.temp + '/site/'))
})

// Duplicate html pages
gulp.task('duplicate_error_html', function(){
  return gulp.src([paths.src + '/site/error/error-factory-creation.html',paths.src + '/site/error/browser-not-supported.html'])
  .pipe(gulp.dest(paths.temp + '/site/war/factory/error/'))
})

gulp.task('stage_cfg', function(){
  return gulp.src(paths.config + buildConfig.jekyllStageConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
})

gulp.task('css_stage', ['copy_src'], function() {
  return gulp.src(paths.temp+'site/styles/*.scss')
  .pipe(compass({
    //config_file: './compass-config.rb',
    css: paths.temp +'site/styles',
    sass: paths.temp +'site/styles'
  }))
  .pipe(gulp.dest(paths.stage + 'site/styles/'));
});
// Ensure waiting for Jekill job finishing
gulp.task('jekyll_stage',['copy_src','stage_cfg'], function () {
         console.log('Jekyll stage......... ');
   return gulp.src(paths.temp+'_config.yml', {read: false})
    .pipe(shell([
      'jekyll build'
    ], {
      cwd: 'target/temp',
      templateData: {
        f: function (s) {
          return s.replace(/$/, '.bak')
        }
      }
    }))
});

gulp.task('copy_stage',['copy_src','stage_cfg','css_stage','jekyll_stage'], function(){
  gulp.src([paths.stage+'/**/*.html', // all HTML
    '!'+paths.stage+'site/custom_pages/**/*.html',
    '!'+paths.stage+'site/admin.html',
    '!'+paths.stage+'site/email-templates_onpremises/*.html',
    paths.stage+'**/*.js',
    paths.stage+'**/*.css',
    paths.stage+'**/*.jpg',
    paths.prod+'**/*.ico',
    paths.stage+'**/*.png',
    paths.stage+'**/*.svg',
    paths.stage+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'stage'));
});


// --------------------------- Building On-premises CCCIS (LDAP version) ----------------------------- 
//----------------
//----------
gulp.task('enterprise',
  ['copy_src',
  'enterprise_cfg',
  'css_enterprise',
  'jekyll_enterprise',
  'copy_enterprise',
  'enterprise_login_page',
  'copy_email_templates',
  'del_replaced_files_ldap',
  'enterprise_copy_templates'],
  function(){
})

gulp.task('enterprise_cfg', function(){
  console.log(buildConfig.jekyllEEConfig);
  return gulp.src(paths.config + buildConfig.jekyllEEConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
})

// Copy enterprise login custom page
gulp.task('enterprise_login_page', ['copy_src','enterprise_cfg','css_enterprise','del_replaced_files_ldap','jekyll_enterprise'], function(){
    console.log(paths.enterprise + 'site/custom_pages/cccis/login.html');
  return   gulp.src(paths.enterprise + 'site/custom_pages/cccis/login.html')
  .pipe(gulp.dest(paths.enterprise+'site'))
  .pipe(rename('index.html'))
  .pipe(gulp.dest(paths.enterprise))
  .pipe(print(function(filepath) {
    return "Copy enterprise login pages to ->" + filepath;
  }));
});

// Cleaning files and folders to be replaced with custom ones
gulp.task('del_replaced_files_ldap',['copy_src','enterprise_cfg','css_enterprise','jekyll_enterprise'], function(){
  return del([
    paths.enterprise+'site/login.html',
    paths.enterprise+'index.html'
    ]);
});

// Copy enterprise custom pages
gulp.task('enterprise_copy_templates', ['copy_src','enterprise_cfg','css_enterprise','clean_templates_ldap','del_replaced_files_ldap','jekyll_enterprise'], function(){
  return   gulp.src(paths.enterprise + 'site/custom_pages/cccis/templates/*.html')
  .pipe(gulp.dest(paths.enterprise+'site/templates'))
  .pipe(print(function(filepath) {
    return "Copy enterprise custom pages to ->" + filepath;
  }));
});

gulp.task('css_enterprise', ['copy_src'], function() {
  return gulp.src(paths.temp+'site/styles/*.scss')
  .pipe(compass({
    //config_file: './compass-config.rb',
    css: paths.temp +'site/styles',
    sass: paths.temp +'site/styles'
  }))
  .pipe(gulp.dest(paths.enterprise + 'site/styles/'));
});
// Ensure waiting for Jekill job finishing
gulp.task('jekyll_enterprise',['copy_src','enterprise_cfg'], function () {
         console.log('Jekyll enterprise......... ');
   return gulp.src(paths.temp+'_config.yml', {read: false})
    .pipe(shell([
      'jekyll build'
    ], {
      cwd: 'target/temp',
      templateData: {
        f: function (s) {
          return s.replace(/$/, '.bak')
        }
      }
    }))
});

gulp.task('copy_enterprise',
  ['copy_src',
  'enterprise_cfg',
  'css_enterprise',
  'jekyll_enterprise',
  'clean_templates_ldap',
  'del_replaced_files_ldap',
  'enterprise_login_page',
  'enterprise_copy_templates'], function(){
  gulp.src([paths.enterprise+'**/*.html', // all HTML
    '!'+paths.enterprise+'site/custom_pages/**/*.html',
    '!'+paths.enterprise+'site/email-templates_onpremises/*.html',
    '!'+paths.enterprise+'site/email-templates/*.html',
    '!'+paths.enterprise+'site/recover-password.html',
    '!'+paths.enterprise+'site/setup-password.html',
    '!'+paths.enterprise+'index.html',
    paths.enterprise+'**/*.js',
    paths.enterprise+'**/*.css',
    paths.enterprise+'**/*.jpg',
    paths.prod+'**/*.ico',
    paths.enterprise+'**/*.png',
    paths.enterprise+'**/*.svg',
    paths.enterprise+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'enterprise'));
});

// Copy omprem email templates page
gulp.task('copy_email_templates', ['copy_src','enterprise_cfg','css_enterprise','jekyll_enterprise','copy_enterprise'], function(){
  return   gulp.src(paths.enterprise+'site/email-templates_onpremises/*.html')
    .pipe(gulp.dest(paths.dist+'enterprise/site/email-templates'))
});

// clean prod templates
gulp.task('clean_templates_ldap',['copy_src','enterprise_cfg','css_enterprise','jekyll_enterprise'], function(){
  return gulp.src(paths.enterprise + '/site/templates/')
  .pipe(rimraf());
});

//***************************************************************** the end On-premises (LDAP version)

// --------------------------- Building On-premises SE (standart edition version) ----------------------------- path.onpremSE
//----------------
//----------
gulp.task('onprem_se',
  ['copy_src', //copy src to /target/temp
  'onprem_se_cfg', //copy _config.yml to /target/temp
  'duplicate_html', //duplicate login.html as /index.html
  'css_onprem_se', //processing scss by compass styles and copy to /site/styles
  'jekyll_onprem_se', //building pages
  'clean_templates_se', //clean up /site/templates/
  'rjs_se', //buld amd-app.js file
  'rev-se', //versioning amd-app.js
  'copy_onprem_se', //copy onprem-se site to /dist/onprem-se
  'onprem_copy_templates', //copy html templates to /site/template
  'copy_onprem_se_email_templates', //copy onprem-se email templates to /dist/onprem-se/site/email-templates
  'del_replaced_files', //delete replaced files
  'onprem_login_page'], //copy onprem custom login page to /onprem-se/site
  function(){

})

// Copy onprem custom pages
gulp.task('onprem_copy_templates', ['copy_src','onprem_se_cfg','css_onprem_se','jekyll_onprem_se','clean_templates_se'], function(){
  return   gulp.src(paths.onpremSE + 'site/custom_pages/onprem-se/templates/*.html')
  .pipe(gulp.dest(paths.onpremSE+'/site/templates'))
  .pipe(print(function(filepath) {
    return "Copy onprem-se custom pages to ->" + filepath;
  }));
});

// Cleaning files and folders to be replaced with custom ones
gulp.task('del_replaced_files',['copy_src','onprem_se_cfg','css_onprem_se','duplicate_html','jekyll_onprem_se'], function(){
  return del([
    paths.onpremSE+'/site/login.html',
    paths.onpremSE+'/index.html'
    ]);
});

// Copy onprem login custom page
gulp.task('onprem_login_page', ['copy_src','onprem_se_cfg','css_onprem_se','clean_templates_se','del_replaced_files','jekyll_onprem_se','duplicate_html'], function(){
  return   gulp.src(paths.onpremSE + 'site/custom_pages/onprem-se/login.html')
  .pipe(gulp.dest(paths.onpremSE+'/site'))
  .pipe(rename('index.html'))
  .pipe(gulp.dest(paths.onpremSE))
  .pipe(print(function(filepath) {
    return "Copy onprem-se login pages to ->" + filepath;
  }));
});

// clean prod templates
gulp.task('clean_templates_se',['copy_src','onprem_se_cfg','css_onprem_se','jekyll_onprem_se'], function(){
  return gulp.src(paths.onpremSE + '/site/templates/')
  .pipe(rimraf());
});

gulp.task('onprem_se_cfg', function(){
  return gulp.src(paths.config + buildConfig.jekyllOnpremSEConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
});

gulp.task('css_onprem_se', ['copy_src'], function() {
  return gulp.src(paths.temp+'site/styles/*.scss')
  .pipe(compass({
    css: paths.temp +'site/styles',
    sass: paths.temp +'site/styles'
  }))
  .pipe(gulp.dest(paths.onpremSE + 'site/styles/'));
});

// Ensure waiting for Jekill job finishing
gulp.task('jekyll_onprem_se',['copy_src','onprem_se_cfg'], function () {
         console.log('Jekyll onpremSE......... ');
   return gulp.src(paths.temp+'_config.yml', {read: false})
    .pipe(shell([
      'jekyll build'
    ], {
      cwd: 'target/temp',
      templateData: {
        f: function (s) {
          return s.replace(/$/, '.bak')
        }
      }
    }))
});

// Builds projects using require.js's optimizer + Minify files with UglifyJS
gulp.task('rjs_se',['copy_src','jekyll_onprem_se','onprem_copy_templates'], function(){
      return  rjs({
            mainConfigFile: paths.onpremSE +'site/scripts/main.js',
            //optimize: 'none', //hardcoded in requirejs plugin
            baseUrl: paths.onpremSE + 'site/scripts',
            wrap: true,
            name: 'main',
            mainFile: paths.onpremSE+'site/index.html',
            out: 'amd-main.js'
        })
      .pipe(print(function(filepath) {
        return "rjs -> built: " + filepath;
      }))
      .pipe(gulp.src(paths.onpremSE +'site/scripts/vendor/require.js'))
      .pipe(reverse({objectMode: true})) // requirejs should be at the begining
      .pipe(concat('amd-app.js'))
      .pipe(uglify())
      .pipe(gulp.dest(paths.onpremSE + 'site/scripts'));
 });

// Replaces references with reved names
  gulp.task ('rev-se', ['copy_src','css_onprem_se','rjs_se','jekyll_onprem_se','onprem_copy_templates'], function(){
    return gulp.src([paths.onpremSE + 'site/scripts/amd-app.js', paths.onpremSE + 'site/styles/*.css'],{base:paths.onpremSE})
    //.pipe(wait(1500))
    .pipe(rev())
    .pipe(print(function(filepath) {
        return "rev -> built: " + filepath;
      }))
    .pipe(gulp.dest(paths.onpremSE))
    .pipe(rev.manifest())
    .pipe(print(function(filepath) {
        return "manifest -> built: " + filepath;
      }))
    .pipe(gulp.dest(paths.onpremSE));
  });

// Start replacing rev references
gulp.task('replace-se',['copy_src','css_onprem_se', 'rjs_se','jekyll_onprem_se','onprem_copy_templates','rev-se'], function(){

  var manifest = require(paths.onpremSE+'rev-manifest.json')
  console.log('Process rev-manifest.json: \n');
  console.log(manifest);
  return gulp.src([paths.onpremSE+'**/*.html'])
  .pipe(replaceRevRef({
    base: paths.onpremSE, //process.cwd()+'/target/prod',
    manifest: manifest,
    path: ''/*,
    cdnPrefix: 'http://absolute.path/cdn/'*/
  }))
  .pipe(gulp.dest(paths.onpremSE));

});

// Removes <!-- build:js ... blocks from html for prod configeration
gulp.task('rmbuild-se', ['copy_src','css_onprem_se','rjs_se','jekyll_onprem_se','onprem_copy_templates','rev-se','replace-se'], function(){
  return gulp.src(paths.onpremSE+'**/*.html')
  .pipe(useref())
  .pipe(print(function(filepath) {
        return "remove <!-- build:js ... blocks -> built: " + filepath;
      }))
  .pipe(gulp.dest(paths.onpremSE));

});

gulp.task('copy_onprem_se',
  ['copy_src',
  'onprem_se_cfg',
  'css_onprem_se',
  'jekyll_onprem_se',
  'onprem_copy_templates',
  'onprem_login_page',
  'rjs_se',
  'rev-se',
  'replace-se',
  'rmbuild-se',
  'del_replaced_files'
  ], function(){
  gulp.src([paths.onpremSE+'**/*.html', // all HTML
    '!'+paths.onpremSE+'site/custom_pages/**/*.html',
    '!'+paths.onpremSE+'site/admin.html',
    '!'+paths.onpremSE+'site/email-templates_onpremises/*.html',
    '!'+paths.onpremSE+'site/email-templates/*.html',
    paths.onpremSE+'**/amd-app*.js', // minified JS
    paths.onpremSE+'**/*.css', // minified CSS
    paths.onpremSE+'**/*.jpg',
    paths.onpremSE+'**/*.ico',
    paths.onpremSE+'**/*.png',
    paths.onpremSE+'**/*.svg',
    paths.onpremSE+'**/modernizr.custom.*.js',
    paths.onpremSE+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'onprem-se'));
});

// Copy omprem email templates page
gulp.task('copy_onprem_se_email_templates', ['copy_src','onprem_se_cfg','css_onprem_se','jekyll_onprem_se'], function(){
  return   gulp.src(paths.onpremSE+'site/email-templates_onpremises/*.html')
    .pipe(gulp.dest(paths.dist+'onprem-se/site/email-templates'))
});

//***************************************************************** On-premises (base version)

// --------------------------- Building GitHub pages (localhost:8080) -----------------------------
//----------------
//----------
gulp.task('gh',['copy_src','gh_cfg','css_gh','jekyll_gh','copy_gh'], function(){
  console.log('Update localhost .....');
})

gulp.task('gh_cfg', function(){
  return gulp.src(paths.config + buildConfig.jekyllGHConfig)
  .pipe(rename('_config.yml'))
  .pipe(gulp.dest(paths.temp))
})

gulp.task('css_gh', ['copy_src'], function() {
  return gulp.src(paths.temp+'site/styles/*.scss')
  .pipe(compass({
    //config_file: './compass-config.rb',
    css: paths.temp +'site/styles',
    sass: paths.temp +'site/styles'
  }))
  .pipe(gulp.dest(paths.gh + 'site/styles/'));
});

gulp.task('jekyll_gh',['copy_src','gh_cfg'], function () {
         console.log('Jekyll ......... ');
     return require('child_process')
        .spawn('jekyll', ['build'], {stdio: 'inherit', cwd: paths.temp});

});

gulp.task('copy_gh',['copy_src','gh_cfg','css_gh','jekyll_gh'], function(){
  gulp.src([paths.gh+'/**/*.html', // all HTML
    paths.gh+'**/*.js',
    paths.gh+'**/*.css',
    paths.gh+'**/*.jpg',
    paths.gh+'**/*.png',
    paths.gh+'**/*.svg',
    paths.gh+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'gh'));
});

gulp.task('watch', function(){
  console.log('Watching for changes in : '+paths.src+'*.*');
return gulp.watch(''+paths.src+'**/*.*',['gh']);


});

// --------------------------- Dev config for LiveReload localhost:8080) -----------------------------
//----------------
//----------
//  Set Up LiveReload (port 35729 which LiveReload uses by default)

gulp.task('express', function() {
  var express = require('express');
  var app = express();
  app.use(require('connect-livereload')({port: 4002}));
  app.use(express.static(__dirname));
  app.listen(4000);
});

var tinylr;
gulp.task('livereload', function() {
  tinylr = require('tiny-lr')();
  tinylr.listen(4002);
});

function notifyLiveReload(event) {
  var fileName = require('path').relative(__dirname, event.path);

  tinylr.changed({
    body: {
      files: [fileName]
    }
  });
}

gulp.task('__watch', function() {
  gulp.watch(''+paths.src+'site/styles/*.scss', ['css_gh']);
  gulp.watch(''+paths.src+'site/**/*.html', ['jekyll_gh']);
  gulp.watch(''+paths.gh+'**/*.html', notifyLiveReload);
  gulp.watch(''+paths.gh+'site/styles/*.css', notifyLiveReload);
});

gulp.task('lr', ['css_gh', 'express', 'livereload', 'watch'], function() {

});

// -------------------- Utils ------------------------
