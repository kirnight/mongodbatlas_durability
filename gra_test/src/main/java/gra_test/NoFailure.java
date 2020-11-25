package gra_test;

public class NoFailure implements Failure {

    @Override
    public void InduceAsync() {
        System.out.println("NofailureInduce");
    }

    @Override
    public void FixAsync() {
        System.out.println("NofailureFix");
    }
}
