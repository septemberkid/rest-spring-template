package leaf;

import leaf.exception.ValidationException;

import java.util.ArrayList;

public class Validator {
    public ArrayList<Boolean> rules = new ArrayList<>();
    public ArrayList<String> messages = new ArrayList<>();

    public Validator(){}
    public static Validator instance() { return new Validator(); }

    public Validator rule(boolean rule, String message){
        this.rules.add(rule);
        this.messages.add(message);
        return this;
    }

    public Object[] validate(){
        boolean res = true;
        ArrayList<String> messages = new ArrayList<>();

        for(int i=0; i<this.rules.size(); i++) if(!this.rules.get(i)){
            res = false;
            messages.add(this.messages.get(i));
        }

        Object[] result = new Object[2];
        result[0] = res;
        result[1] = messages;
        return result;
    }

    public void check() throws ValidationException {
        Object[] result = this.validate();
        if(!(Boolean) result[0]){
            ArrayList<String> messages = (ArrayList<String>) result[1];
            String msg = String.join("; ",messages);
            throw new ValidationException(msg);
        }
    }

    public String inspect() throws ValidationException {
        String msg = null;
        Object[] result = this.validate();
        if(!(Boolean) result[0]){
            ArrayList<String> messages = (ArrayList<String>) result[1];
            msg = String.join("; ",messages);
        }
        return msg;
    }

    public static boolean required(Object value){
        return value != null && !String.valueOf(value).equals("");
    }

    public static boolean requiredIfExist(Object value){
        return value == null || !value.equals("");
    }

    public static boolean equals(Object value1, Object value2){
        return value1.equals(value2);
    }
}
