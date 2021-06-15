package org.sakaiproject.ims.cc;

public enum CcVersion {
    CC10(100),
    CC11(110),
    CC12(120),
    CC13(130);

    private final int version;

    CcVersion(int version) {
        this.version = version;
    }

    public CcVersion getVersion() {
        return CcVersion.values()[version];
    }

    public boolean equals(CcVersion CCVersion) {
        return this.version == CCVersion.version;
    }

    public boolean greaterThan(CcVersion CCVersion) {
        return this.version > CCVersion.version;
    }

    public boolean lessThan(CcVersion CCVersion) {
        return this.version < CCVersion.version;
    }

    public boolean greaterThanOrEqualTo(CcVersion CCVersion) {
        return (equals(CCVersion) || greaterThan(CCVersion));
    }

    public boolean lessThanOrEqualTo(CcVersion CCVersion) {
        return (equals(CCVersion) || lessThan(CCVersion));
    }

}
