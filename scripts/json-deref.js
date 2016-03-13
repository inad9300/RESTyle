// USE:
// nodejs json-deref.js <schema-file> [[<pretty>] <output-file>]

var fs = require('fs');
var deref = require('json-schema-deref');
var schema = require(process.argv[2]);
 
deref(schema, function(err, fullSchema) {
    if (err)
        return console.log(err);

    var pretty = process.argv[3];
    var filename = process.argv[4];

    var fullSchemaStr = pretty === '1' || pretty === 'true'
                      ? JSON.stringify(fullSchema, null, 4)
                      : JSON.stringify(fullSchema);

    if (filename) {
        fs.writeFile(filename, fullSchemaStr, function (err) {
            if (err)
                return console.log(err);
        });
    } else
        console.log(fullSchemaStr);
});