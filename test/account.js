const Q = require('q');
const expect = require('chai').expect;
const request = require('request');

describe('account', function(){
    it('create', function(done) { // !!! do not change "function" to an arrow
        this.timeout(25000);

        let req = {
            url  : 'http://192.168.1.236:8080/account',
            json : {
                account_id : 1,
                email : 'despotix3@gmail.com',
                username : 'Despotix3',
                password : 'test1',
                legal_name : 'Legal',
                language_code : 'eng',
                country_code : 'US',
                timezone : 'America/Los_Angeles'
            }
        };

        Q.npost(request, 'post', [req])
        .then((r) => {
            let [res, json] = r;
            expect(json.email).to.equal(req.json.email);
            done();
        }).catch( (err)=>{
            done(err);
        });
    });
});