package org.jetbrains.contest.keypromoter;

import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by athiele on 05.01.2015.
 */
public class KeyPromoterToolWindowFactory implements ToolWindowFactory {

  private JButton refreshButton = new JButton("Refresh");

  private JLabel test = new JLabel("Welcome to your Key Promoter stats");
  private JPanel toolWindowContent = new JPanel();
  private JList topTenList;

  public KeyPromoterToolWindowFactory() {

    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        test.setText("Refresher!");
      }
    });
  }
  
  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

    toolWindowContent.setLayout(new GridLayout(3,1));
    toolWindowContent.add(test);


    String[] topTen = new String[10];
    topTen[0] = "First";
    topTen[1] = "Second";

    topTenList = new JBList(topTen);

    toolWindowContent.add(topTenList);
    toolWindowContent.add(refreshButton);

    Content content = contentFactory.createContent(toolWindowContent, "", false);
    toolWindow.getContentManager().addContent(content);
    
  }
}
