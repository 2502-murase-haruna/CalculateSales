package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_SEQUENTIAL = "売上ファイル名が連番になっていません";
	private static final String TOO_MANY_ZEROES = "合計金額が10桁を超えました";
	private static final String THIS_FILE_INVALID_CODE = "の支店コードが不正です";
	private static final String THIS_FILE_INVALID_FORMAT = "のフォーマットが不正です";


	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//コマンドライン引数が1つ設定されていなかった場合は、
	    //エラーメッセージをコンソールに表示します。
		if(args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
		}
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)

		// listFilesを使用してfilesという配列に、
		// 指定したパスに存在する全てのファイル（または、ディレクトリ）の情報を格納します。
		File[] files = new File(args[0]).listFiles();

		// 先にファイルの情報を格納するList(ArrayList)を宣言します。
		List<File> rcdFiles = new ArrayList<>();

		// filesの数だけ繰り返すことで、
		// 指定したパスに存在する全てのファイル(または、ディレクトリ)の数だけ繰り返されます
		for(int i = 0; i < files.length; i++) {

			//matches を使用して対象がファイルであり、ファイル名が「数字8桁.rcd」なのか判定します。
			String fileName = files[i].getName();
			if(files[i].isFile() && fileName.matches("^[0-9]{8}.rcd$")) {
				//売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
				rcdFiles.add(files[i]);
			}
		}
		//売上ファイルを保持しているListをソートする
		Collections.sort(rcdFiles);
		//売上ファイルのファイル名が連番になっているか確認
		for(int j = 0; j < rcdFiles.size() - 1; j++) {

			int former = Integer.parseInt(rcdFiles.get(j).getName().substring(0,8));
			int latter = Integer.parseInt(rcdFiles.get(j + 1).getName().substring(0,8));
			//比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を比較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表⽰します。
				System.out.println(FILE_NOT_SEQUENTIAL);
				return;
			}
		}

		BufferedReader br = null;
		for(int i = 0; i < rcdFiles.size(); i++) {
			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);
				String line;

				// 先にファイルの情報を格納するList(ArrayList)を宣言します。
				List<String> rcdInfoFiles = new ArrayList<>();

				while((line = br.readLine()) != null) {
					//Map に追加する情報をputの引数として指定します。
					rcdInfoFiles.add(line);
				}

				//売上ファイルの行数が2行ではなかった場合は、エラーメッセージをコンソールに表示します。
				if(rcdInfoFiles.size() != 2) {
					System.out.println(rcdFiles.get(i).getName() + THIS_FILE_INVALID_FORMAT);
				}
				//支店情報を保持しているMapに売上ファイルの支店コードが存在しなかった場合は、
			    //エラーメッセージをコンソールに表示します。
				if(!branchNames.containsKey(rcdInfoFiles.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + THIS_FILE_INVALID_CODE);
				}

				//売上ファイルから読み込んだ売上金額を加算していくために、型の変換を行います。
				long fileSale = Long.parseLong(rcdInfoFiles.get(1));
				//売上⾦額が数字ではなかった場合は、エラーメッセージをコンソールに表示します。
				if(!rcdInfoFiles.get(1).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
				}
				//読み込んだ売上⾦額を加算します。
				Long saleAmount = branchSales.get(rcdInfoFiles.get(0)) + fileSale;
				//売上⾦額が11桁以上の場合、エラーメッセージをコンソールに表示します。
				if(saleAmount >= 10000000000L) {
					System.out.println(TOO_MANY_ZEROES);
				}
				//加算した売上⾦額をMapに追加します。
				branchSales.put(rcdInfoFiles.get(0), saleAmount);

			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}

		}


		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//支店定義ファイルの存在チェック
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}



			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//splitを使って「,」（カンマ）で分割すると、
				//items[0]には支店コード、items[1]には支店名が格納されます。
				String[]items = line.split(",");

				//Map に追加する2つの情報をputの引数として指定します。
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);

				//支店定義ファイルが仕様のフォーマットか確認
				//1行に支店コードと支店名が記載されており、支店コードが数字３桁であるか
				if((items.length != 2) || (!items[0].matches("^[0-9]{3}$"))) {
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}
			}



		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for(String key : branchNames.keySet()) {
				//支店コード
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}

		return true;
	}

}
