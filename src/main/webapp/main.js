(function(){

    
    blocks = new Blocks();

    blocks.types.addCompatibility('string', 'number');
    blocks.types.addCompatibility('string', 'bool');
    blocks.types.addCompatibility('bool', 'number');
    blocks.types.addCompatibility('bool', 'integer');
    blocks.types.addCompatibility('bool', 'string');

    blocks.run('#blocks');
    blocks.load({"blocks":[],"edges":[]});

    function saveToFile(text) {
        var pom = document.createElement('a');
        pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
        pom.setAttribute('download', "export.json");

        if (document.createEvent) {
            var event = document.createEvent('MouseEvents');
            event.initEvent('click', true, true);
            pom.dispatchEvent(event);
        }
        else {
            pom.click();
        }
    }

    blocks.ready(function() {
	blocks.menu.addAction('Export', function(blocks) {
        saveToFile(JSON.stringify(blocks.export(),null,4));
	}, 'export');

        $('.setLabel').click(function() {
            for (k in blocks.edges) {
                var edge = blocks.edges[k];
                edge.setLabel('Edge #'+edge.id);
            }
        });

        $('.setInfos').click(function() {
            for (k in blocks.blocks) {
                var block = blocks.blocks[k];
                block.setInfos('Hello, I am the block #'+block.id);
            }
        });

        $('.setDescriptions').click(function() {
            for (k in blocks.blocks) {
                var block = blocks.blocks[k];
                block.setDescription('This is the block #'+block.id);
            }
        });

        $('.resize').click(function() {
            $('#blocks').width('300px');
            blocks.perfectScale();
        });

        $('.hideIcons').click(function() {
            blocks.showIcons = false;
            blocks.redraw();
        });

        $('.loadBlocks').click(function() {

                var file=document.createElement("input");
                file.setAttribute("type","file");
                $(file).change(
                    function (e) {
                        var fr = new FileReader();
                        fr.onload=function(e){
                            blocks.register(JSON.parse(e.target.result));
                        }
                        fr.readAsText(e. target. files[0]);
                    }
                );

                if (document.createEvent) {
                  var event = document.createEvent('MouseEvents');
                  event.initEvent('click', true, true);
                  file.dispatchEvent(event);
                }
                else {
                  file.click();
                }
        });
        $('#open').click(function() {

            var file=document.createElement("input");
            file.setAttribute("type","file");
            $(file).change(
                function (e) {
                    var fr = new FileReader();
                    fr.onload=function(e){
                        blocks.load(JSON.parse(e.target.result));
                    }
                    fr.readAsText(e. target. files[0]);
                }
            );

            if (document.createEvent) {
                var event = document.createEvent('MouseEvents');
                event.initEvent('click', true, true);
                file.dispatchEvent(event);
            }
            else {
                file.click();
            }
        });

        $('#save').click(function () {
            saveToFile(JSON.stringify(blocks.export(),null,4));
        });

        $("#fileUploadForm").submit(function (event) {

            event.preventDefault();
            // Get form
            var form = $('#fileUploadForm')[0];

            // Create an FormData object
            var data = new FormData(form);

            // If you want to add an extra field for the FormData
            //data.append("CustomField", "This is some extra data, testing");

            $.ajax({
                type: "POST",
                enctype: 'multipart/form-data',
                url: "rest/workflow/upload",
                data: data,
                processData: false,
                contentType: false,
                cache: false,
                timeout: 600000,
                success: function (data) {

                    blocks.register(JSON.parse(data));

                },
                error: function (e) {

                    alert("Error!"+e.responseText);

                }
            });

        });

        $("#execute").click(function(event){

            // Create an FormData object
            var data = new FormData();

            // If you want to add an extra field for the FormData
            data.append("workflow", JSON.stringify(blocks.export()));

            $.ajax({
                type: "POST",
                enctype: 'multipart/form-data',
                url: "rest/workflow/execute",
                data: data,
                processData: false,
                contentType: false,
                cache: false,
                timeout: 600000,
                success: function (data) {
                    data = JSON.parse(data);
                    console.log(data);
                    for (var k in blocks.blocks) {
                        var block = blocks.blocks[k];

                        for(var x in data){
                            if(data[x].id===block.id){
                                if(data[x].output){
                                    block.setInfos('Previous Output:'+data[x].output);
                                }
                            }
                        }

                    }

                    // blocks.load(data);

                },
                error: function (e) {

                    alert("Error!"+e.responseText);

                }
            });
        });

    });
})();
