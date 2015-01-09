package org.jetbrains.contest.keypromoter;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by athiele on 05.01.2015.
 */
public class KeyPromoterToolWindowFactory implements ToolWindowFactory {

  private JButton refreshButton = new JButton("Refresh");
  private KeyPromoterPersistentStats statsService = ServiceManager.getService(KeyPromoterPersistentStats
          .class);

  private JLabel test = new JLabel("Welcome to your Key Promoter stats");
  private JPanel toolWindowContent = new JPanel();
  private JList topTenList;
  private String[] topTen = new String[10];

  public KeyPromoterToolWindowFactory() {

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateStats();
        topTenList.setListData(topTen);
      }
    });
  }
  
  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {

    updateStats();

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();


    toolWindowContent.setLayout(new GridLayout(3, 1));
    toolWindowContent.add(test);
    topTenList = new JBList(topTen);

    toolWindowContent.add(topTenList);
    toolWindowContent.add(refreshButton);

    Content content = contentFactory.createContent(toolWindowContent, "", false);
    toolWindow.getContentManager().addContent(content);
    
  }

  private void updateStats() {
    Map<String, Integer> stats = statsService.getStats();
    List<Entry<String, Integer>> sortedEntries = sortByValues(stats);

    int i = 0;
    for (Entry entry : sortedEntries) {

      topTen[i] = entry.getKey() + " - " + entry.getValue();


      i++;
      if (i == 9) break;

    }

  }

  static <K,V extends Comparable<? super V>>
  List<Entry<K, V>> sortByValues(Map<K,V> map) {

    List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

    Collections.sort(sortedEntries,
            new Comparator<Entry<K, V>>() {
              @Override
              public int compare(Entry<K, V> e1, Entry<K, V> e2) {
                return e2.getValue().compareTo(e1.getValue());
              }
            }
    );

    return sortedEntries;
  }


}
