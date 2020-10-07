package uk.co.markg.mimic.database;

public class DeleteService {

  private ChannelRepository channelRepo;
  private UsageRepository usageRepo;
  private UserRepository userRepo;
  private ServerConfigRepository serverConfigRepo;

  public DeleteService() {
    this.channelRepo = ChannelRepository.getRepository();
    this.usageRepo = UsageRepository.getRepository();
    this.userRepo = UserRepository.getRepository();
    this.serverConfigRepo = ServerConfigRepository.getRepository();
  }

  public void deleteServer(long serverid) {
    channelRepo.deleteByServerId(serverid);
    usageRepo.deleteByServerId(serverid);
    userRepo.deleteByServerId(serverid);
    serverConfigRepo.delete(serverid);
  }

}
