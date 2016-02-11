package es.berry.restyle.main;

import org.apache.commons.cli.CommandLine;

// FIXME: type concept should be reviewed (and the concept itself!)

public abstract class CodeGenerator {

    public final static int DATABASE = 0;
    public final static int SERVER = 1;
    public final static int CLIENT = 2;

    protected int type;

    protected CodeGenerator nextHandler;

    public void setNextHandler(CodeGenerator next) {
        this.nextHandler = next;
    }

    public void handle(int type, CommandLine cmd) {
        if (this.type == type) {
            this.generate(cmd);
        }
        if (this.nextHandler != null) {
            this.nextHandler.handle(type, cmd);
        }
    }

    abstract protected void generate(CommandLine cmd);
}
