var gulp = require('gulp'),
    tinylr = require('tiny-lr'), // Mini webserver for livereload
    cleanCSS = require('gulp-clean-css'), // CSS minifying
    shell = require('gulp-shell'),
    print = require('gulp-print'),
    jshint = require('gulp-jshint'),
    stylish = require('jshint-stylish'),
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
    watch = require('gulp-watch'),
    del = require('del');               // Remove files and folders

var buildConfig = {
        jekyllStageConfig : "_config.stage.yml", //saas-stage
        jekyllOnpremSEConfig : "_config.onprem-se.yml" //onprem-se
    };

var paths = {
        src: 'app/',
         // assembly folders
        onpremSE: './target/onprem-se/',
        stage: './target/stage/',
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

gulp.task('lint',function(){
  gulp.src(['!'+paths.src+'site/scripts/vendor/*.*',paths.src+'site/scripts/**/*.js'])
    .pipe(jshint())
  .pipe(jshint.reporter('jshint-stylish'))
  .pipe(jshint.reporter('fail'))
});

// --------------------------- Building Stage -----------------------------
//----------------
//----------
gulp.task('stage',['copy_src','stage_cfg','css_stage','jekyll_stage','copy_stage'], function(){

})
// Copies src to temp folder
gulp.task('copy_src', ['lint','duplicate_html','duplicate_error_html'], function(){
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
    '!'+paths.stage+'site/admin.html',
    '!'+paths.stage+'site/email-templates_onpremises/*.html',
    paths.stage+'**/*.js',
    paths.stage+'**/*.css',
    paths.stage+'**/*.jpg',
    paths.prod+'**/*.ico',
    paths.stage+'**/*.png',
    paths.stage+'**/*.svg',
    paths.stage+'**/*.json',
    paths.stage+'**/*.woff',
    paths.stage+'**/*.woff2',
    paths.stage+'**/*.ttf',
    paths.stage+'**/*.eot',
    paths.stage+'**/*.otf',
    paths.stage+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'stage'));
});

// --------------------------- Building On-premises SE (standart edition version) ----------------------------- path.onpremSE
//----------------
//----------
gulp.task('onprem_se',
  ['copy_src', //copy src to /target/temp
  'onprem_se_cfg', //copy _config.yml to /target/temp
  'duplicate_html', //duplicate login.html as /index.html
  'css_onprem_se', //processing scss by compass styles and copy to /site/styles
  'jekyll_onprem_se', //building pages
  'rjs_se', //buld amd-app.js file
  'rev-se', //versioning amd-app.js
  'copy_branding',//copy branding folder
  'copy_onprem_se'], //copy onprem-se site to /dist/onprem-se
  function(){

})

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
gulp.task('rjs_se',['copy_src','jekyll_onprem_se'], function(){
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
  gulp.task ('rev-se', ['copy_src','css_onprem_se','rjs_se','jekyll_onprem_se'], function(){
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
gulp.task('replace-se',['copy_src','css_onprem_se', 'rjs_se','jekyll_onprem_se','rev-se'], function(){

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
gulp.task('rmbuild-se', ['copy_src','css_onprem_se','rjs_se','jekyll_onprem_se','rev-se','replace-se'], function(){
  return gulp.src(paths.onpremSE+'**/*.html')
  .pipe(useref())
  .pipe(print(function(filepath) {
        return "remove <!-- build:js ... blocks -> done: " + filepath;
      }))
  .pipe(gulp.dest(paths.onpremSE));

});
// copy branding folder
gulp.task('copy_branding', ['copy_onprem_se'], function(){
  gulp.src([paths.onpremSE+'site/branding/**/*.* '])
  .pipe(gulp.dest(paths.dist+'onprem-se/site/branding/'));
});
gulp.task('copy_onprem_se',
  ['copy_src',
  'onprem_se_cfg',
  'css_onprem_se',
  'jekyll_onprem_se',
  'rjs_se',
  'rev-se',
  'replace-se',
  'rmbuild-se'
  ], function(){
  gulp.src([paths.onpremSE+'**/*.html', // all HTML
    '!'+paths.onpremSE+'site/admin.html',
    paths.onpremSE+'**/amd-app-*.js', // minified JS
    paths.onpremSE+'**/*-*.css', // minified CSS
    paths.onpremSE+'**/*.jpg',
    paths.onpremSE+'**/*.ico',
    paths.onpremSE+'**/*.png',
    paths.onpremSE+'**/*.svg',
    paths.onpremSE+'**/*.woff',
    paths.onpremSE+'**/*.woff2',
    paths.onpremSE+'**/*.ttf',
    paths.onpremSE+'**/*.eot',
    paths.onpremSE+'**/*.otf',
    paths.onpremSE+'**/modernizr.custom.*.js',
    paths.onpremSE+'**/*.txt'  // robots.txt
    ])
  .pipe(gulp.dest(paths.dist+'onprem-se'));
});

//***************************************************************** On-premises (base version)

// --------------------------- Dev config for LiveReload localhost:8080) -----------------------------
//----------------
//----------
//  Set Up LiveReload (port 35729 which LiveReload uses by default)

// This task creates local server
gulp.task('connect', ['stage'], function() {
  connect.server({
    root: paths.dist+"/stage",
    livereload: true,
    port: 9000
  });
});

gulp.task('watch', function() {
  gulp.watch(paths.src+'/**/*.*', ['stage']);
  watch(paths.dist+"/stage").pipe(connect.reload());
});
// 
gulp.task('lr',['connect', 'watch'],function(){

});
// -------------------- Utils ------------------------
