
require('reflect-metadata');

var smc = require('../../../target/js/service-modal.component'),
    diffusion = require('diffusion');

describe('Service modal component', function() {
    it('can be created', function() {
        var component = new smc.ServiceModalComponent();
        expect(component).toBeDefined();
    });
});
