package window;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.*;

public class FRT_Window extends JFrame implements ActionListener {
	private static final long serialVersionUID = 2967533240915562582L;

	private JTextPane tpWorkingDirectryPath;
	private JTextPane tpTargetFileList;
	private JTextPane tpRegex;
	private JTextPane tpReplacement;
	private JSpinner spnIDStart;
	private JSpinner spnIncliment;
	private JSpinner spnSufLen;
	private JButton btnOpenFolder;
	private JButton btnCheck;
	private JButton btnExecute;

	private String[] filepath;

	public FRT_Window() {
		layoutComponents();
		initialSettings();
	}

	private void initialSettings() {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception ex) {
			System.out.println("Error L&F Setting");
			ex.printStackTrace();
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("File Rename Tool");
		setSize(600, 500);
		setVisible(true);
	}

	private void layoutComponents() {
		tpWorkingDirectryPath = new JTextPane();
		tpRegex = new JTextPane();
		tpTargetFileList = new JTextPane();
		tpReplacement = new JTextPane();

		btnOpenFolder = new JButton("フォルダを開く");
		btnOpenFolder.addActionListener(this);
		btnOpenFolder.setActionCommand(CMD_OPEN);

		btnCheck = new JButton("チェック");
		btnCheck.addActionListener(this);
		btnCheck.setActionCommand(CMD_CHECK);

		btnExecute = new JButton("ファイル名を変更する");
		btnExecute.addActionListener(this);
		btnExecute.setActionCommand(CMD_RENAME);

		spnIDStart = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
		spnIDStart.setEditor(new JSpinner.NumberEditor(spnIDStart));

		spnIncliment = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
		spnIncliment.setEditor(new JSpinner.NumberEditor(spnIncliment));

		spnSufLen = new JSpinner(new SpinnerNumberModel(2, 1, null, 1));
		spnSufLen.setEditor(new JSpinner.NumberEditor(spnSufLen));

		Container baseContainer = getContentPane();
		baseContainer.setLayout(new BorderLayout());

		// ヘッダー: フォルダパス＆正規表現
		JPanel panelHeader = new JPanel();
		baseContainer.add(BorderLayout.NORTH, panelHeader);
		panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));

		Box panelWorkDir = Box.createHorizontalBox();
		panelHeader.add(panelWorkDir);
		panelWorkDir.add(new JLabel("作業フォルダパス"));
		panelWorkDir.add(tpWorkingDirectryPath);

		btnOpenFolder.setPreferredSize(new Dimension(100, 20));
		panelWorkDir.add(btnOpenFolder);

		Box panelRegex = Box.createHorizontalBox();
		panelHeader.add(panelRegex);
		panelRegex.add(new JLabel("正規表現"));
		panelRegex.add(tpRegex);
		btnCheck.setPreferredSize(new Dimension(100, 20));
		panelRegex.add(btnCheck);

		Box panelReplacement = Box.createHorizontalBox();
		panelHeader.add(panelReplacement);
		panelReplacement.add(new JLabel("置き換えるファイル名"));
		panelReplacement.add(tpReplacement);

		Box panelFileIndex = Box.createHorizontalBox();
		panelHeader.add(panelFileIndex);
		panelFileIndex.add(new JLabel("開始インデックス"));
		panelFileIndex.add(spnIDStart);
		panelFileIndex.add(new JLabel("インデックス増分"));
		panelFileIndex.add(spnIncliment);
		panelFileIndex.add(new JLabel("インデックス文字数"));
		panelFileIndex.add(spnSufLen);

		panelHeader.setSize(600, 60);

		// TargetFileList
		JPanel panelFileList = new JPanel();
		baseContainer.add(BorderLayout.CENTER, panelFileList);
		panelFileList.setLayout(new BorderLayout());
		panelFileList.add(BorderLayout.WEST, new JLabel("マッチしたファイル"));
		panelFileList.add(BorderLayout.CENTER, tpTargetFileList);

		// 変換するボタン
		JPanel panelFooter = new JPanel();
		baseContainer.add(BorderLayout.SOUTH, panelFooter);
		panelFooter.add(btnExecute);
		panelFooter.setSize(600, 20);

	}

	private static final String CMD_OPEN = "Openfile";
	private static final String CMD_CHECK = "Check";
	private static final String CMD_RENAME = "Rename";

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case CMD_OPEN:
			openFolder();
			break;
		case CMD_CHECK:
			checkFileMatched();
			break;
		case CMD_RENAME:
			renameFiles();
			break;
		default:
			break;
		}
	}

	private void openFolder() {
		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int selected = filechooser.showOpenDialog(this);
		if (selected == JFileChooser.APPROVE_OPTION) {
			if (filechooser.getSelectedFile().exists()) {
				tpWorkingDirectryPath.setText(filechooser.getSelectedFile().getPath());
			} else {
				JOptionPane.showMessageDialog(this, "フォルダが存在しません。");
			}
		}
	}

	private void checkFileMatched() {
		String pathWorkingDir = tpWorkingDirectryPath.getText();

		if (!new File(pathWorkingDir).exists()) {
			JOptionPane.showMessageDialog(this, "フォルダが存在しません。");
			return;
		}

		String regex = tpRegex.getText();

		Stream<File> fileArray = Arrays.stream(new File(pathWorkingDir).listFiles());

		// フォルダ内のファイルをすべて取得する
		fileArray = fileArray.filter(file -> file.isFile());

		// 正規表現にマッチするファイルを取得する
		fileArray = fileArray.filter(file -> Pattern.compile(regex).matcher(file.getName()).find());

		// ファイルの絶対パスを作成し、配列に変換する
		filepath = fileArray.map(file -> file.getAbsolutePath()).toArray(String[]::new);

		// 表示用の文字列の作成
		String str = "";
		for (String s : filepath) {
			str += s + "\n";
		}
		tpTargetFileList.setText(str);

	}

	private void renameFiles() {
		File workDir = new File(tpWorkingDirectryPath.getText());
		if (!workDir.exists()) {
			JOptionPane.showMessageDialog(this, "フォルダが存在しません。", "エラー", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int suffixLen = (Integer) spnSufLen.getValue();
		if (!(suffixLen > 0)) {
			JOptionPane.showMessageDialog(this, "インデックス長さは正数のみ有効です。", "エラー", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int inclements = (Integer) spnIncliment.getValue();
		if (!(inclements > 0)) {
			JOptionPane.showMessageDialog(this, "インクリメントは正数のみ有効です。", "エラー", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (filepath == null || filepath.length == 0) {
			JOptionPane.showMessageDialog(this, "リネーム対象ファイルがありません。", "エラー", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int fileIdx = (Integer) spnIDStart.getValue();
		
		String result = "";
		for (final String path : filepath) {
			// ファイル名を作成（置き換えファイル名＋インデックス＋拡張子）
			String indexFormat = "%0" + Integer.toString(suffixLen) + "d";
			String newFileName = tpReplacement.getText() + String.format(indexFormat, fileIdx) + "."
					+ getExtention(new File(path));

			// ディレクトリパスとファイル名を結合
			Path newFilePath = workDir.toPath().resolve(newFileName);

			// 置き換え処理
			new File(path).renameTo(new File(newFilePath.toString()));
			fileIdx += inclements;

			result += new File(path).getName() + " -> " + newFileName + "\n";
		}

		JOptionPane.showMessageDialog(this, result);
	}

	private String getExtention(File file) {
		String fileName = file.getName();
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}
}
