var tree;
var blocks;

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
                        blocks.clear();
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

        $('#clear').click(function () {
            alertify.confirm('Workflow Desginer','Clear Workflow?', function(){ blocks.clear(); }, function(){});
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
                    initializeTree();
                    alertify.notify(newBlocks.length + ' blocks registered', 'success', 5);
                },
                error: function (e) {
                    alertify.notify('Error Registering blocks', 'error', 5);
                }
            });

        });

        $("#execute").click(function(event){
            alertify.notify('Workflow Execution Started', 'success', 5);

            for (k in blocks.blocks) {
                var block = blocks.blocks[k];
                block.setInfos('');
            }

            document.getElementById("modals").innerHTML="";

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
                                    var output = 'Previous Output:';
                                     if(data[x].output){
                                         var outputObj = data[x].output;
                                         if (outputObj.type==="STRING"){
                                             output+=outputObj.value;
                                         }
                                         else if (outputObj.type==="FILE"){
                                             output+="<a href=\"rest/workflow/file/"+outputObj.value.filename+"\">"+outputObj.value.title+"</a>";
                                         }
                                         else if (outputObj.type==="TABLE"){
                                             output+="<br/>"+
                                                 '<a href="rest/workflow/file/'+outputObj.value.filename+'"><button class="btn btn-success btn-sm" >Download Table</button></a>'+
                                                 '<a href="csv.html?csv='+outputObj.value.filename+'" target="_blank"><button class="btn btn-success btn-sm" >Open</button></a>';

                                         }
                                         else if (outputObj.type==="GRAPH"){
                                             var modal = document.getElementById("graphModal");
                                             var div = document.createElement('span');
                                             div.innerHTML=modal.innerHTML;
                                             div.getElementsByClassName("modal fade")[0].setAttribute("id", "graphModal"+block.id);
                                             div.getElementsByClassName("modal-body")[0].setAttribute("id", "graphDiv"+block.id);
                                             document.getElementById("modals").innerHTML+=div.innerHTML;
                                             output+="<br/>"+
                                                 '<button class="btn btn-primary btn-sm" href="#"  data-toggle="modal" data-target="#graphModal'+block.id+'">Show Graph</button>';
                                             var traces=[];
                                             for(var p=0;p < outputObj.value.traces.length ;p++){
                                                 traces.push(outputObj.value.traces[p]);
                                             }
                                             Plotly.newPlot('graphDiv'+block.id, traces, outputObj.value.layout);
                                         }
                                     }
                                     if(data[x].stdout || data[x].stderr){
                                         var modal = document.getElementById("logModal");
                                         var div = document.createElement('span');
                                         div.innerHTML=modal.innerHTML;
                                         div.getElementsByClassName("stdout")[0].innerHTML=data[x].stdout;
                                         div.getElementsByClassName("stderr")[0].innerHTML=data[x].stderr;
                                         div.getElementsByClassName("modal fade")[0].setAttribute("id", "logModal"+block.id);
                                         document.getElementById("modals").innerHTML+=div.innerHTML;
                                         output+="<br/>"+
                                             '<button class="btn btn-primary btn-sm" href="#"  data-toggle="modal" data-target="#logModal'+block.id+'">Show Log</button>';
                                     }

                                    block.setInfos(output);
                                }
                            }
                        }

                    }
                    alertify.notify('Workflow Execution Completed!', 'success', 5);
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


        //Creating the tree


        initializeTree()

    });



})();

function initializeTree() {
    tree = createTree('div_tree','white',contex_menu);

    //Rendering the tree
    tree.drawTree();

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

            //Loop to create test nodes
            for (var jar in library) {
                node1 = tree.createNode(jar,false,'animara/images/star.png',null,null,'context1');
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

function allowDrop(ev) {
    ev.preventDefault();
}

function drag(ev,node_id) {
    var rel = getBlockRel(tree,node_id);
    for(var meta in blocks.metas){
        if(blocks.metas[meta].name===rel){
            ev.dataTransfer.setData("rel", rel);
            return;
        }
    }
    ev.preventDefault();
}

function drop(ev) {
    var rel = ev.dataTransfer.getData("rel");
    var blocksPosition = $("#blocks").offset();
    blocks.addBlock(rel, ev.clientX-blocksPosition.left-blocks.center.x, ev.clientY-blocksPosition.top-blocks.center.y);
}

function getBlockRel(tree,node_id){
    var node=tree;
    for(var child in node.childNodes){
        if(node.childNodes[child].id==node_id){
            return node.childNodes[child].text;
        }
        else{
            var inChildren = getBlockRel(node.childNodes[child],node_id);
            if(inChildren)return inChildren;
        }
    }
}
