package prr.tariffs;

import java.io.Serializable;

import prr.communications.TextCommunication;
import prr.communications.VideoCommunication;
import prr.communications.VoiceCommunication;

public interface TariffPlan extends Serializable {
    
    double taxTextCommunication(TextCommunication comm);

    double taxVoiceCommunication(VoiceCommunication comm);

    double taxVideoCommunication(VideoCommunication comm);

}
