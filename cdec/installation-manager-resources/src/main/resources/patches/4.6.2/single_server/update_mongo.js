var fixSnapshots = function() {
    var organizationDb = db.getSiblingDB('organization');
    organizationDb.snapshots.update({}, { $rename: {"isDev" : "dev"} }, false, true);
}

fixSnapshots();
