describe("app-module", function(){

	var _module;

	beforeEach(function(){
		_module = angular.module("appModule");
	});

	it("should be registered", function(){
		expect(_module).not.to.equal(false);
	});

	describe("Dependencies:", function() {

		var deps;
		var hasModule = function(m) {
			return deps.indexOf(m) >= 0;
		};

		beforeEach(function() {
			deps = _module.value('appName').requires;
		});

		//you can also test the module's dependencies
		it("should have the ng dependencies", function() {
			expect(hasModule('ngRoute')).to.equal(true);
			expect(hasModule('ngTouch')).to.equal(true);
			expect(hasModule('ngAnimate')).to.equal(true);
		});

	});

});

describe("pc-navigation-controller", function () {

	/**
	 * SETUP =========================================================
	 */

	var _controller;
	var _rootScope;
	var _scope;

	beforeEach(module("appModule"));
	beforeEach(inject(function beforeEach($controller, $rootScope) {
		_rootScope = $rootScope;
		_scope = $rootScope.$new();
		_controller = $controller("PCNavigationController", {$scope: _scope});
		sinon.spy(_scope, 'pushSection');
		sinon.spy(_scope, 'popSection');
	}));

	/**
	 * TEARDOWN ======================================================
	 */

	afterEach(function () {

	});

	/**
	 * TESTS =========================================================
	 */

	describe("initialization", function () {
		it("should start with sizes setup", function () {
			expect(_scope.sizes).to.not.equal(null);
		});

		it("should start with root section as Postcard", function () {
			expect(_scope.rootSection.title).to.equal("Postcard");
			expect(_scope.rootSection.slug).to.equal("postcard");
		});

		it("should one item on the stack and it should be rootSection", function () {
			expect(_scope.stack.length).to.equal(1);
			expect(_scope.stack[0]).to.equal(_scope.rootSection);
		});

		it("should not be sending", function () {
			expect(_scope.isSending).to.equal(false);
		});

		it("should have all methods defined", function(){
			expect(typeof _scope.getWidth).to.equal('function');
			expect(typeof _scope.getHeight).to.equal('function');
			expect(typeof _scope.pushSection).to.equal('function');
			expect(typeof _scope.popSection).to.equal('function');
			expect(typeof _scope.back).to.equal('function');
			expect(typeof _scope.send).to.equal('function');
		});
	});

	describe("behaviour", function(){
		it("should push / pop section on broadcast of 'pushSection'/'popSection", function(){
			_rootScope.$broadcast('pushSection', 'tester');
			_scope.pushSection.should.have.been.calledWith('tester');
		});

		it("should pop section on broadcast of 'popSection'", function(){
			_rootScope.$broadcast('popSection');
			_scope.popSection.should.have.been.calledWith();
		});
	});

});
