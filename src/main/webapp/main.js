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

        $('#undo').click(function() {
            blocks.history.restoreLast();
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

        $("#importSubmit").click(function (event) {

            event.preventDefault();
            // Get form

            // Create an FormData object
            var formData = new FormData(document.getElementById('fileUploadForm'));


            // If you want to add an extra field for the FormData
            //data.append("CustomField", "This is some extra data, testing");

            $.ajax({
                type: "POST",
                enctype: 'multipart/form-data',
                url: "rest/workflow/upload",
                data: formData,
                processData: false,
                contentType: false,
                cache: false,
                timeout: 600000,
                success: function (data) {
                    var newBlocks = JSON.parse(data);
                    blocks.register(newBlocks);
                    alertify.notify(newBlocks.length + ' blocks registered', 'success', 5);
                },
                error: function (e) {
                    alertify.notify('Error Registering blocks', 'error', 5);
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
                },
                error: function (e) {
                    alertify.notify(e.responseText, 'error', 5);
                }
            });
        });

    });
})();
