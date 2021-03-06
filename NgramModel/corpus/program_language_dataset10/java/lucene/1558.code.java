package org.apache.lucene.index;
import java.util.List;
public final class KeepOnlyLastCommitDeletionPolicy implements IndexDeletionPolicy {
  public void onInit(List<? extends IndexCommit> commits) {
    onCommit(commits);
  }
  public void onCommit(List<? extends IndexCommit> commits) {
    int size = commits.size();
    for(int i=0;i<size-1;i++) {
      commits.get(i).delete();
    }
  }
}
