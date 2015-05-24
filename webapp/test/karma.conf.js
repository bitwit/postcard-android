module.exports = function (config) {
	config.set({
		// Karma configuration

		// base path, that will be used to resolve files and exclude
		basePath: '../',

		frameworks: ['mocha', 'sinon-chai'],

		// test results reporter to use
		// possible values: dots || progress || growl
		reporters: ['progress', 'coverage'],
		// Start these browsers, currently available:
		// - Chrome
		// - ChromeCanary
		// - Firefox
		// - Opera
		// - Safari (only Mac)
		// - PhantomJS
		// - IE (only Windows)
		browsers: ['Chrome'], //, 'Safari', 'Firefox'],
		// enable / disable watching file and executing tests whenever any file changes
		autoWatch: true,

		// Continuous Integration mode
		// if true, it capture browsers, run tests and exit
		singleRun: false,

		// enable / disable colors in the output (reporters and logs)
		colors: true,

		// list of files / patterns to load in the browser
		files: [
			'../app/bower_components/angular/angular.js',
			'../app/bower_components/angular-animate/angular-animate.js',
			'../app/bower_components/angular-route/angular-route.js',
			'../app/bower_components/angular-touch/angular-touch.js',
			'../app/bower_components/angular-mocks/angular-mocks.js',
			'../public/js/*.js',
			'/unit/**/*.js'
		],

		preprocessors: {
			// source files, that you wanna generate coverage for
			// do not include tests or libraries
			// (these files will be instrumented by Istanbul)
			'public/js/*.js': ['coverage']
		},

		coverageReporter: {
			type : 'html',
			dir : 'test/reports/coverage/'
		}

	});
};
