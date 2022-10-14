package ru.netology.printParam;

import ru.netology.Server;

public class ParamPrintImpl implements ParamPrinter {
    private Server server;

    public ParamPrintImpl(Server server) {
        this.server = server;
    }

    @Override
    public void printParam() {
        try {
            server.getQueue().take().
                    getQueryParams().
                    forEach(x -> System.out.println(x.getName() + " " + x.getValue()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printParam(String name) {
        try {
            System.out.println(server.getQueue().take().
                    getQueryParam(name));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
