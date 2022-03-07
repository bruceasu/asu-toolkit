package me.asu.actor;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface Manager {

    Actor createActor(Class<? extends Actor> clazz, String name);

    Actor createAndStartActor(Class<? extends Actor> clazz, String name);

    Actor createActor(Class<? extends Actor> clazz,
                      String name,
                      Map<String, Object> options);

    Actor createAndStartActor(Class<? extends Actor> clazz,
                              String name,
                              Map<String, Object> options);

    void startActor(Actor a);

    void detachActor(Actor actor);

    int send(Message message, Actor from, Actor to);

    int send(Message message, Actor from, Actor[] to);

    int send(Message message, Actor from, Collection<Actor> to);

    int send(Message message, Actor from, String category);

    int broadcast(Message message, Actor from);

    Set<String> getCategories();

    void terminateAndWait();

    void terminate();

    int getActorCount(Class type);

    Actor[] getActors();

    void free(Actor actor);
}
