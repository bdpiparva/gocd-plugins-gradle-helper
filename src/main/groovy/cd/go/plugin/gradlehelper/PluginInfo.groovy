package cd.go.plugin.gradlehelper

class PluginInfo {
    String id
    String name
    String group
    String version
    String goCdVersion
    String vendorName
    String vendorUrl

    Map<String, String> toHash() {
        return ['id'         : id,
                'name'       : name,
                'group'      : group,
                'version'    : version,
                'vendorUrl'  : vendorUrl,
                'vendorName' : vendorName,
                'goCdVersion': goCdVersion
        ]
    }
}
