package components;

public class CardLite {
    private final String name;
    private final String descr;
    private final String list;

    public CardLite(String n, String d, String l){
        name  = n;
        descr = d;
        list = l;
    }

    public String getName(){
        return name;
    }

    public String getDescr(){
        return descr;
    }

    public String getList(){
        return list;
    }
}
