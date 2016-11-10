
require('reflect-metadata');

var seg = require('../../../target/js/session-establishment.guard'),
    when = require('saywhen');

describe('Session establishment guard', function() {
    var diffusionService;
    var router;
    var guard;
    var session;

    beforeEach(function() {
        diffusionService = jasmine.createSpyObj('diffusionService', ['get']);
        router = jasmine.createSpyObj('router', ['navigate']);
        session = jasmine.createSpyObj('session', ['isConnected']);

        guard = new seg.SessionEstablishmentGuard(router, diffusionService);
    });

    it('can be created', function() {
        expect(guard).toBeDefined();
    });

    it('presents login page when no session', function(done) {
        when(diffusionService.get).isCalled.thenReturn(Promise.reject(new Error('No session')));

        guard.canActivate().then((result) => {
            expect(result).toBe(false);
            expect(router.navigate).toHaveBeenCalled();
            done();
        }, done.fail);
    });

    it('presents login page when session is not connected', function(done) {
        when(session.isConnected).isCalled.thenReturn(false);
        when(diffusionService.get).isCalled.thenReturn(Promise.resolve(session));

        guard.canActivate().then((result) => {
            expect(result).toBe(false);
            expect(router.navigate).toHaveBeenCalled();
            done();
        }, done.fail);
    });

    it('allows activation when session is connected', function(done) {
        when(session.isConnected).isCalled.thenReturn(true);
        when(diffusionService.get).isCalled.thenReturn(Promise.resolve(session));

        guard.canActivate().then((result) => {
            expect(result).toBe(true);
            done();
        }, done.fail);
    });
});
