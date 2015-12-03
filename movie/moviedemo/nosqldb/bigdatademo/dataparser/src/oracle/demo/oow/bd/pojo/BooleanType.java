package oracle.demo.oow.bd.pojo;

import oracle.demo.oow.bd.util.StringUtil;

public enum BooleanType {
    YES("Y"),
    NO("N");

    private String type;

    BooleanType(String type) {
        this.type = type;
    }

    public String getValue() {
        switch (this) {
        case YES:
            type = "Y";
            break;
        case NO:
            type = "N";
            break;
        }

        return type;
    }


    public static BooleanType getType(String type) {
        BooleanType bType = null;
        if (StringUtil.isNotEmpty(type)) {
            for (BooleanType bt : BooleanType.values()) {
                if (bt.getValue().equalsIgnoreCase(type)) {
                    bType = bt;
                    break;
                }
            } //EOF for
        }//EOF if (StringUtil.isNotEmpty(type)) {
        return bType;
    } //valueOf


    public static void main(String[] args) {
        BooleanType type = BooleanType.YES;
        System.out.println(type.getValue());
    }
}
