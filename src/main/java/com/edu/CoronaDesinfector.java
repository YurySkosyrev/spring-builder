package com.edu;

public class CoronaDesinfector {

    private Announcer announcer = ObjectFactory.getInstance().createObject(Announcer.class);
    private Policeman policeman = ObjectFactory.getInstance().createObject(Policeman.class);

    public void start(Room room){
        announcer.announce("Начинаем дезинфекцию - все вон!");
        policeman.makePeopleLeaveRoom();
        desinfect(room);
        announcer.announce("Рискните войти обратно!");
    }

    private void desinfect(Room room) {
        System.out.println("Зачитывается молитва: - корона изыди! и вирус низвергнут в ад");
    }
}
