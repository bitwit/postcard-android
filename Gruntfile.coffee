#global module:no

module.exports = (grunt) ->
  # Project configuration.

  require('time-grunt')(grunt);

  grunt.initConfig({
    #
    # Metadata.
    #
    meta:
      version: '0.1.0'

    dir:
      webapp: 'webapp/public'
      tmp: '.tmp'

    banner: '/* Copyright (c) <%= grunt.template.today("yyyy") %> ' + 'Kyle Newsome Licensed MIT */\n'
    #
    # Task configuration.
    #
    coffee:
      compile:
        options:
          banner: '<%= banner %>',
          bare: yes
        files: [
          {
            '<%= dir.tmp %>/js/app.js':[
              '<%= dir.webapp %>/client.coffee'
              '<%= dir.webapp %>/**/**.coffee'
            ]
          }
        ]

    jade:
      compile:
        options:
          data:
            debug: no
        files:
          "<%= dir.tmp %>/index.html": ["<%= dir.webapp %>/index.jade"]

    html2js:
      options:
        module: 'postcard-templates'
        rename: (moduleName) ->
          name = moduleName.split('/')
          name = name[name.length - 1].replace('.jade', '');
      main:
        src: ['webapp/public/*/**/**.jade'],
        dest: '.tmp/js/templates.js'

    concat:
      styles:
        src: ['<%= dir.webapp %>/config.sass', '<%= dir.webapp %>/style.sass', '<%= dir.webapp %>/**/_*.sass'],
        dest: '<%= dir.tmp %>/style.sass'

    compass:
      options:
        sassDir: '<%= dir.tmp %>'
        cssDir: '<%= dir.tmp %>'
        generatedImagesDir: '<%= dir.tmp %>/img'
        imagesDir: '<%= dir.webapp %>/img'
        javascriptsDir: '<%= dir.webapp %>'
        relativeAssets: no
        assetCacheBuster: no
        raw: 'Sass::Script::Number.precision = 10\n'
      main:
        options:
          generatedImagesDir: '<%= dir.tmp %>/img'

    clean:
      android: ["postcard/src/main/assets/www"]
      tmp: ["<%= dir.tmp %>"]

    copy:
      tmp:
        files: [{
          expand: yes
          flatten: yes
          dot: yes
          cwd: 'webapp/app/bower_components'
          dest: '<%= dir.tmp %>/vendor'
          src: 'angular*/*.min.js*'
        },{
          expand: yes
          flatten: yes
          dot: yes
          cwd: 'webapp/app/data'
          dest: '<%= dir.tmp %>/data'
          src: '*.json'
        },{
          expand: yes
          flatten: yes
          dot: yes
          cwd: '<%= dir.webapp %>/img'
          dest: '<%= dir.tmp %>/img'
          src: '*.*'
        }]
      android:
        files: [{
          expand: yes
          cwd: '<%= dir.tmp %>'
          dest: 'postcard/src/main/assets/www'
          src: '**/**'
        },{
          expand: yes
          cwd: '.'
          dest: 'postcard/src/main/assets'
          src: 'postcard.properties'
        }]

    concurrent:
      server:
        tasks: ['watch','shell:server']
        options:
          logConcurrentOutput: yes

    shell:
      server:
        command: "./node_modules/http-server/bin/http-server <%= dir.tmp %> -o"

    watch:
      options:
        livereload: 1337
      scripts:
        files: ['webapp/**/*.coffee']
        tasks: ['coffee']
      css:
        files: ['webapp/**/*.sass']
        tasks: ['concat:styles','compass']
      jade:
        files: ['webapp/**/*.jade']
        tasks: ['jade', 'html2js']

  })

  # These plugins provide necessary tasks.
  grunt.loadNpmTasks 'grunt-spritesmith'
  grunt.loadNpmTasks 'grunt-contrib-clean'
  grunt.loadNpmTasks 'grunt-contrib-coffee'
  grunt.loadNpmTasks 'grunt-contrib-jade'
  grunt.loadNpmTasks 'grunt-contrib-compass'
  grunt.loadNpmTasks 'grunt-contrib-concat'
  grunt.loadNpmTasks 'grunt-contrib-copy'
  grunt.loadNpmTasks 'grunt-contrib-watch'
  grunt.loadNpmTasks 'grunt-html2js'
  grunt.loadNpmTasks 'grunt-concurrent'
  grunt.loadNpmTasks 'grunt-git'
  grunt.loadNpmTasks 'grunt-shell'

  grunt.registerTask 'build', [
    'clean:tmp'
    'coffee'
    'concat:styles'
    'compass',
    'jade',
    'html2js'
    'copy:tmp'
  ]

  grunt.registerTask 'web', [
    'build'
    'concurrent'
  ]

  grunt.registerTask 'android', [
    'build'
    'clean:android'
    'copy:android'
  ]

