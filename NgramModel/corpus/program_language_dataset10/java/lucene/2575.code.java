package org.apache.solr.update;
public class RollbackUpdateCommand extends UpdateCommand {
  public RollbackUpdateCommand() {
    super("rollback");
  }
}
