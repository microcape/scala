// © 2010 EPFL/LAMP
// code by Gilles Dubochet

function Scheduler() {
    var scheduler = this;
    var resolution = 1;
    this.timeout = undefined;
    this.queues = new Array(0); // an array of work pacakges indexed by index in the labels table.
    this.labels = new Array(0); // an indexed array of labels indexed by priority. This should be short.
    this.label = function(name, priority) {
        this.name = name;
        this.priority = priority;
    }
    this.addLabel = function(name, priority) {
        var idx = 0;
        while (idx < scheduler.queues.length && scheduler.labels[idx].priority <= priority) { idx = idx + 1; }
        scheduler.labels.splice(idx, 0, new scheduler.label(name, priority));
        scheduler.queues.splice(idx, 0, new Array(0));
    }
    this.clearLabel = function(name) {
        var idx = 0;
        while (idx < scheduler.queues.length && scheduler.labels[idx].name != name) { idx = idx + 1; }
        if (idx < scheduler.queues.length && scheduler.labels[i].name == name) {
            scheduler.labels.splice(idx, 1);
            scheduler.queues.splice(idx, 1);
        }
    }
    this.nextWork = function() {
        var fn = undefined;
        var idx = 0;
        while (idx < scheduler.queues.length && scheduler.queues[idx].length == 0) { idx = idx + 1; }
        if (idx < scheduler.queues.length && scheduler.queues[idx].length > 0) {
            var fn = scheduler.queues[idx].shift();
        }
        return fn;
    }
    this.add = function(labelName, self, fn) {
        var doWork = function() {
            scheduler.timeout = setTimeout(function() {
                var work = scheduler.nextWork();
                if (work != undefined) {
                    work[1].call(work[0]);
                    doWork();
                }
                else {
                    scheduler.timeout = undefined;
                }
            }, resolution);
        }
        var idx = 0;
        while (idx < scheduler.labels.length && scheduler.labels[idx].name != labelName) { idx = idx + 1; }
        if (idx < scheduler.queues.length && scheduler.labels[idx].name == labelName) {
            scheduler.queues[idx].push([self, fn]);
            if (scheduler.timeout == undefined) {
                doWork();
            }
        }
        else {
            throw("queue for add is non existant");
        }
    }
    this.clear = function(labelName) {
        var idx = 0;
        while (idx < scheduler.labels.length && scheduler.labels[idx].name != labelName) { idx = idx + 1; }
        if (idx < scheduler.queues.length && scheduler.labels[idx].name == labelName) {
            scheduler.queues[idx] = new Array();
        }
    }
};
