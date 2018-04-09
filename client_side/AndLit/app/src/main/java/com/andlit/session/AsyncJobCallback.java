package com.andlit.session;

// this callback class is created to give developer flexibility in what to do when a
// certain async thread has finished running. The result object is result obtained
// from what the thread has ended up with
public abstract class AsyncJobCallback {
    private Object[] args;
    public AsyncJobCallback(Object ... args){ this.args = args; }
    public abstract Object run(Object result);
}
