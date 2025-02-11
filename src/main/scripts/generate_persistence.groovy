String basePackage = properties['basePackage']

sourcePath = properties['output'] + "/${basePackage.replace('.', '/')}"

files = new File(properties['entities']).listFiles()
files.each {
    println "Entity definition: $it.path"

    tableName = it.name.replace('.json','')
    className = toCamelCase(tableName, true)
    entityDefs = parseJson(it.text)

    templateFiles = new File(properties['templates']).listFiles({File f -> f.isFile()} as FileFilter)
    templateFiles.each {
        println "Template definition: $it.path"
        templateName = toCamelCase(it.name.replace('.java', ''))

        templateContent = new groovy.text.SimpleTemplateEngine().createTemplate(it.text)
                .make(['basePackage': basePackage, 'className': className, 'tableName': tableName, 'columns': entityDefs])
        outFile = new File(sourcePath + "/${templateName.toLowerCase()}/${className}${templateName.capitalize()}.java")
        outFile.getParentFile().mkdirs()
        outFile.newWriter()
        outFile << templateContent
    }
};

staticFiles = new File(properties['templates'] + "/statics").listFiles({File f -> f.isFile()} as FileFilter)
staticFiles.each {
    println "Static template definition: $it.path"
    templateContent = new groovy.text.SimpleTemplateEngine().createTemplate(it.text)
            .make(['basePackage': basePackage])
    outFile = new File(sourcePath + "/${it.name}")
    outFile.getParentFile().mkdirs()
    outFile.newWriter()
    outFile << templateContent
}

def parseJson(String text) {
    dataTypes = ['java.sql.Timestamp': 'Timestamp',
                 'java.util.Date': 'Date',
                 'String': 'String',
                 'java.math.BigDecimal':'BigDecimal']

    def entityJson = new groovy.json.JsonSlurper().parseText(text)
    for (col in entityJson) {
        col.attr = toCamelCase(col.column)
        col.column = col.column.toLowerCase()
        if (col.mappingType == null) {
            def datatype = dataTypes[col.javaType];
            if (datatype != null) {
                col.mappingType = datatype;
            }
        }
    }

    return entityJson
}

String toCamelCase(String text, boolean capitalized = false ) {
    text = text.toLowerCase()
    text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
    return capitalized ? text.capitalize() : text
}