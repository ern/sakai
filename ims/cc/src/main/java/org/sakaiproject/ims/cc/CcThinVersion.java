package org.sakaiproject.ims.cc;

/**
 * Thin CC the value of the ‘xs:version’ attribute is either ‘IMS THIN CC 1.2 CP 1.2’ or ‘IMS THIN CC 1.3 CP 1.2’
 * depending on whether the Thin CC is based on CCv1.2 or CCv1.3 respectively
 */
public enum CcThinVersion {
    THIN_CC12_CP12(120),
    THIN_CC13_CP12(121);

    private final int version;

    CcThinVersion(int version) {
        this.version = version;
    }

    public CcThinVersion getVersion() {
        return CcThinVersion.values()[version];
    }

    public boolean equals(CcThinVersion ccThinVersion) {
        return this.version == ccThinVersion.version;
    }

    public boolean greaterThan(CcThinVersion ccThinVersion) {
        return this.version > ccThinVersion.version;
    }

    public boolean lessThan(CcThinVersion ccThinVersion) {
        return this.version < ccThinVersion.version;
    }

    public boolean greaterThanOrEqualTo(CcThinVersion ccThinVersion) {
        return (equals(ccThinVersion) || greaterThan(ccThinVersion));
    }

    public boolean lessThanOrEqualTo(CcThinVersion ccThinVersion) {
        return (equals(ccThinVersion) || lessThan(ccThinVersion));
    }

}
