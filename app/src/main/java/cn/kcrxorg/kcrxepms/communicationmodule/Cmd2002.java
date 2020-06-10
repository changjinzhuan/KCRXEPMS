package cn.kcrxorg.kcrxepms.communicationmodule;

public class Cmd2002 extends BaseCmd {
    String id;
    String timestamp;
    String data;

    public Cmd2002(BaseCmd baseCmd)
    {
        setId("0"+baseCmd.getPayloads().substring(1,8));
        setTimestamp(baseCmd.getPayloads().substring(8,24));
        setData(baseCmd.getPayloads().substring(24));
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


}
