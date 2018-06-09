var tree;
var blocks;

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
                    initializeTree();
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
                    for (var k in blocks.blocks) {
                        var block = blocks.blocks[k];

                        for(var x in data){
                            if(data[x].id===block.id){
                                if(data[x].output||data[x].stderr||data[x].stdout){
                                    var output = 'Previous Output:'+data[x].output;
                                    if(data[x].stdout){
                                        output+="<br/>"+data[x].stdout;
                                    }
                                    if(data[x].stderr){
                                        if(data[x].stderr.indexOf("Caused by:")>0){
                                            output+="<br/>"+data[x].stderr.split("Caused by:")[1];
                                        }
                                        else
                                            output+="<br/>"+data[x].stdout;
                                    }
                                    block.setInfos(output);
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

    $(document).ready(function () {

        $(".toggle-sidebar").click(function(){
            $("#sidebar").toggleClass("collapsed");
            $("#content").toggleClass("col-md-12 col-md-9");
        });



        //Initializing Tree

        //Tree Context Menu Structure
        var contex_menu = {
            'context1' : {
                elements : [
                    {
                        text : 'Node Actions',
                        icon: 'animara/images/blue_key.png',
                        action : function(node) {

                        },
                        submenu: {
                            elements : [
                                {
                                    text : 'Toggle Node',
                                    icon: 'animara/images/leaf.png',
                                    action : function(node) {
                                        node.toggleNode();
                                    }
                                },
                                {
                                    text : 'Expand Node',
                                    icon: 'animara/images/leaf.png',
                                    action : function(node) {
                                        node.expandNode();
                                    }
                                },
                                {
                                    text : 'Collapse Node',
                                    icon: 'animara/images/leaf.png',
                                    action : function(node) {
                                        node.collapseNode();
                                    }
                                },
                                {
                                    text : 'Expand Subtree',
                                    icon: 'animara/images/tree.png',
                                    action : function(node) {
                                        node.expandSubtree();
                                    }
                                },
                                {
                                    text : 'Collapse Subtree',
                                    icon: 'animara/images/tree.png',
                                    action : function(node) {
                                        node.collapseSubtree();
                                    }
                                },
                                {
                                    text : 'Delete Node',
                                    icon: 'animara/images/delete.png',
                                    action : function(node) {
                                        node.removeNode();
                                    }
                                },
                            ]
                        }
                    },
                    {
                        text : 'Child Actions',
                        icon: 'animara/images/blue_key.png',
                        action : function(node) {

                        },
                        submenu: {
                            elements : [
                                {
                                    text : 'Create Child Node',
                                    icon: 'animara/images/add1.png',
                                    action : function(node) {
                                        node.createChildNode('Created',false,'animara/images/folder.png',null,'context1');
                                    }
                                },
                                {
                                    text : 'Create 1000 Child Nodes',
                                    icon: 'animara/images/add1.png',
                                    action : function(node) {
                                        for (var i=0; i<1000; i++)
                                            node.createChildNode('Created -' + i,false,'animara/images/folder.png',null,'context1');
                                    }
                                },
                                {
                                    text : 'Delete Child Nodes',
                                    icon: 'animara/images/delete.png',
                                    action : function(node) {
                                        node.removeChildNodes();
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        };

        //Creating the tree
        tree = createTree('div_tree','white',contex_menu);

        //Rendering the tree
        tree.drawTree();

        initializeTree()

    });



})();

function initializeTree() {
    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "rest/workflow/initialize",
        processData: false,
        contentType: false,
        cache: false,
        timeout: 600000,
        success: function (data) {
            var blockDefinitions = JSON.parse(data)
            blocks.register(blockDefinitions);

            var library=[];
            for(i=0;i<blockDefinitions.length;i++){

                var jar = blockDefinitions[i].module.split(":")[0];
                var package = blockDefinitions[i].module.split(":")[1];
                var family = blockDefinitions[i].family;
                var name = blockDefinitions[i].name;

                if(!library[jar]){
                    library[jar]=[];
                }
                if(!library[jar][package]){
                    library[jar][package]=[];
                }
                if(!library[jar][package][family]){
                    library[jar][package][family]=[];
                }
                library[jar][package][family].push(name);
            }

            var childNodes=tree.childNodes
            for(var node in childNodes){
                tree.removeNode(node);
            }
            //Loop to create test nodes
            for (var jar in library) {
                node1 = tree.createNode(jar,false,'animara/images/star.png',null,null,'context1');
                console.log(node1);
                for(var package in library[jar]){
                    var node2 = node1.createChildNode(package,false,'animara/images/leaf.png',null,null,'context1');
                    for(var family in library[jar][package]){
                        var node3 = node2.createChildNode(family, false, 'animara/images/blue_key.png',null,'context1');
                        for(var i=0;i<library[jar][package][family].length; i++){
                            node3.createChildNode(library[jar][package][family][i], false, 'animara/images/monitor.png',null,'context1');
                        }
                    }
                }

            }
        },
        error: function (e) {
            alert("Error!"+e.responseText);
        }
    });

}

function expand_all() {
    tree.expandTree();
}


function collapse_all() {
    tree.collapseTree();
}
