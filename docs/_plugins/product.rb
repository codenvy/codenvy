#
# CODENVY CONFIDENTIAL
# ________________
#
# [2016] Codenvy, S.A.
# All Rights Reserved.
# NOTICE: All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any. The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#
# Contributors:
#   Florent Benoit - Initial Implementation

module Jekyll

  class ProductDescription

    def initialize(formal_name, mini_name, name)
      @formal_name = formal_name
      @mini_name = mini_name
      @name = name
    end
  end

  class ProductTag < Liquid::Tag

    def initialize(tag_name, text, tokens)
      super
      @text = text
    end

    def render(context)

      if( !@text.nil? && !@text.empty? )
        productDescription = ProductDescription.new("Codenvy", "codenvy", "CODENVY");
        key = @text.strip;
        key[0] = '@';
        entry = productDescription.instance_variable_get(key);
        return entry;
      end
      return  "###FIXME###";
    end
  end
end

print "\033[1;34m Loading Codenvy Product Plugin\033[0m...";
STDOUT.flush
Liquid::Template.register_tag('product', Jekyll::ProductTag)
puts " \033[0;32m[OK]\033[0m";
