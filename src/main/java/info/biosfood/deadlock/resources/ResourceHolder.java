package info.biosfood.deadlock.resources;

public class ResourceHolder {

    private Resource1 resource1 = new Resource1();
    private Resource2 resource2 = new Resource2();
    private Resource3 resource3 = new Resource3();

    public Resource1 getResource1() {
        return resource1;
    }

    public Resource2 getResource2() {
        return resource2;
    }

    public Resource3 getResource3() {
        return resource3;
    }
}
