package edu.und.seau.presentation.presenters;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.WriterException;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import edu.und.seau.firebase.commands.Command;
import edu.und.seau.firebase.commands.CommandManager;
import edu.und.seau.firebase.commands.enumerations.ControlStatus;
import edu.und.seau.firebase.database.FirebaseDatabaseInterface;
import edu.und.seau.presentation.views.ConnectionScreenView;

import static edu.und.seau.ui.connect_screen.KEY_PASSED_UAV_ID;
import static edu.und.seau.ui.connect_screen.KEY_UAV_NAME;

public class ConnectionScreenPresenter {
    private FirebaseDatabaseInterface databaseInterface;
    private ConnectionScreenView view;
    private SharedPreferences connectionScreenPreferences;
    private CommandManager commandManager;
    private Bundle extraData;
    private ListenerRegistration listenerRegistration;
    private String uavID;

    @Inject
    public ConnectionScreenPresenter(FirebaseDatabaseInterface databaseInterface, CommandManager commandManager)
    {
        this.databaseInterface = databaseInterface;
        this.commandManager = commandManager;
    }

    public void setView(ConnectionScreenView view)
    {
        if(view != null) {
            this.view = view;
            connectionScreenPreferences = view.getSharedPreferences();
            HandleExtraData(view.GetExtraData());
            ListenForControlRequests();
        }
    }

    private void ListenForControlRequests(){
        if(extraData.containsKey(KEY_PASSED_UAV_ID)){
            listenerRegistration = commandManager.ListenForRequests(uavID, this::OnRequestsRecieved);
        }
    }

    private void RemoveControlRequestListener(){
        if(listenerRegistration != null){
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void OnRequestsRecieved(List<Command> commandList){
        Command request = null;
        RemoveControlRequestListener();
        for(Command command : commandList){
            String userID = command.getCommandID();
            if(command.getCommandNumber() == 2){
                if(request == null){
                    request = command;
                    commandManager.SendCommand2Response(uavID,userID, ControlStatus.CONTROL_REQUEST_SUCCESS,null);
                }
                else {
                    commandManager.SendCommand2Response(uavID,userID, ControlStatus.ALREADY_CONTROLLED,null);
                }
            }
            databaseInterface.DeleteRequest(uavID, userID,null);
        }
        if(request != null){
            OnControlRequested(uavID, request.getCommandID());
        }
        else {
            ListenForControlRequests();
        }
    }

    private void OnControlRequested( String uavID, String userID){
        if(view != null){
            view.onConnected(uavID, userID);
        }
    }

    private Bitmap GenerateQRCode(String uavID){
        Bitmap returnValue = null;
        QRGEncoder encoder = new QRGEncoder(uavID,null, QRGContents.Type.TEXT,75);
        try {
            returnValue =  encoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return  returnValue;
    }

    private void HandleExtraData(Bundle extraData){
        if(view != null){
            if(extraData != null){
                this.extraData = extraData;
                if(extraData.containsKey(KEY_PASSED_UAV_ID)){
                    uavID = Objects.requireNonNull(extraData.getCharSequence(KEY_PASSED_UAV_ID)).toString();
                    view.setUavID(uavID);
                    view.setQRCode(GenerateQRCode(uavID));
                }
                if(extraData.containsKey(KEY_UAV_NAME)){
                    String uavName = Objects.requireNonNull(Objects.requireNonNull(extraData.getCharSequence(KEY_UAV_NAME)).toString());
                    view.setUavName(uavName);
                }
            }
            else {
                this.extraData = new Bundle();
            }
        }
    }
}
