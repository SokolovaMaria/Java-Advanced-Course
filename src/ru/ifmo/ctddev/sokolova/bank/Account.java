package ru.ifmo.ctddev.sokolova.bank;

import java.rmi.*;

public interface Account extends Remote {

    public String getId() 
        throws RemoteException;


    public int getAmount() 
        throws RemoteException;


    public void setAmount(int amount) 
        throws RemoteException;
}