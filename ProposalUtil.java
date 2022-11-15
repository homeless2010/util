package com.step.esms.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.alibaba.fastjson.JSON;
import com.sudytech.log.ILog;
import com.sudytech.log.LogFactory;
import com.sudytech.orm2.surpport.CountManager.MDBSession;
import com.sudytech.system.env.CoreplusEnv;
import com.sudytech.util.base.StringUtil;

/**
 * 工具类
 *
 * @author ccj
 */
public class ProposalUtil {
	private static ILog _log = LogFactory.getInstance().getLog(
			ProposalUtil.class);

	/**
	 * 附件下载
	 *
	 * @param list
	 *            文件信息集合
	 */
	public static void downloadFile(List<Map<String, String>> fileList,
			HttpServletResponse response) throws Exception {
		String zipName = "attachment.zip";
		response.setContentType("APPLICATION/OCTET-STREAM");
		if (fileList.size() == 1) {// 单个附件直接下载,设置响应头
			response.setHeader(
					"Content-Disposition",
					"attachment; filename="
							+ URLEncoder.encode(
									fileList.get(0).get("fileName"), "UTF-8"));
		} else {// 多个附件压缩下载,设置响应头
			response.setHeader("Content-Disposition", "attachment; filename="
					+ zipName);
		}
		// 设置压缩流：直接写入response，实现边压缩边下载
		ZipOutputStream zipos = null;
		try {
			if (fileList.size() == 1) {// 单个附件直接下载
//				File file = new File(CoreplusEnv.getRealPath(fileList.get(0)
//						.get("storedPath")));
				File file = new File(CoreplusEnv.getRealPath(fileList.get(0)
						.get("previewUri")));
				FileInputStream fileInputStream = new FileInputStream(file);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						fileInputStream);
				byte[] b = new byte[32768];
				OutputStream outputStream = response.getOutputStream();
				int length = 0;
				while ((length = bufferedInputStream.read(b)) != -1) {
					outputStream.write(b, 0, length);
				}
				bufferedInputStream.close();
				outputStream.flush();
				outputStream.close();
			} else {// 多个附件压缩下载
				zipos = new ZipOutputStream(new BufferedOutputStream(
						response.getOutputStream()));
				zipos.setMethod(ZipOutputStream.DEFLATED);// 设置压缩方法
				DataOutputStream os = null;
				List<Map<String, String>> returnFileList = new ArrayList<Map<String, String>>();
				// 这里递归是防止要下载的文件有重名的导致下载失败
				returnFileList = fileNameToWeight(fileList, returnFileList);
				// 循环将文件写入压缩流
				for (int i = 0; i < returnFileList.size(); i++) {
//					String filePath = CoreplusEnv.getRealPath(returnFileList
//							.get(i).get("storedPath"));
					String filePath = CoreplusEnv.getRealPath(returnFileList
							.get(i).get("previewUri"));
					String fileName = returnFileList.get(i).get("fileName");
					File file = new File(filePath);// 要下载文件的路径
					// 添加ZipEntry，并ZipEntry中写入文件流
					zipos.putNextEntry(new ZipEntry(fileName));
					os = new DataOutputStream(zipos);
					InputStream is = new FileInputStream(file);
					byte[] b = new byte[32768];
					int length = 0;
					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
					}
					is.close();
					zipos.closeEntry();
				}
				// 关闭流
				os.flush();
				os.close();
				zipos.close();
			}
		} catch (Exception e) {
			_log.error("downloadFile error", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * 递归防止文件名重复
	 */
	private static List<Map<String, String>> fileNameToWeight(
			List<Map<String, String>> fileList,
			List<Map<String, String>> returnList) {
		if (fileList.isEmpty()) {
			return returnList;
		}
		Map<String, String> fileDetailMap = fileList.get(0);
		returnList.add(fileDetailMap);
		// remove list
		List<Map<String, String>> removeList = new ArrayList<Map<String, String>>();
		removeList.add(fileDetailMap);
		for (int i = 1; i < fileList.size(); i++) {
			int count = 1;
			if (fileDetailMap.get("fileName").equals(
					fileList.get(i).get("fileName"))) {
				String fileName = fileList.get(i).get("fileName");
				String[] fileNameArray = fileName.split("\\.");
				fileList.get(i).put(
						"fileName",
						fileNameArray[0] + "(" + count + ")."
								+ fileNameArray[1]);
				count++;
				returnList.add(fileList.get(i));
				removeList.add(fileList.get(i));
			}
		}
		fileList.removeAll(removeList);
		fileNameToWeight(fileList, returnList);
		return returnList;
	}

	/**
	 *
	 * @param toSavePath
	 *            要保存的附件json串
	 * @param savedPath
	 *            已保存的固件json串
	 * @return
	 * @throws Exception
	 */
	public static String uploadEditByPath(String toSavePath, String savedPath,
			MDBSession dbSession) throws Exception {
		List<FileInfo> fileList = new ArrayList<FileInfo>();
		List<FileInfo> savedFileInfos = new ArrayList<FileInfo>();
		List<FileInfo> toSaveFileInfos = new ArrayList<FileInfo>();
		if (!StringUtil.isEmpty(toSavePath)) {
			toSaveFileInfos = JSON.parseArray(toSavePath, FileInfo.class);
		}
		if (!StringUtil.isEmpty(savedPath)) {// 编辑
			savedFileInfos = JSON.parseArray(savedPath, FileInfo.class);
			if ((savedFileInfos.size() == toSaveFileInfos.size())
					&& savedFileInfos.containsAll(toSaveFileInfos)) {
				return savedPath;
			}
			List<FileInfo> tempFileInfos1 = new ArrayList<FileInfo>();
			tempFileInfos1.addAll(toSaveFileInfos);
			// 新上传的文件
			toSaveFileInfos.removeAll(savedFileInfos);
			// toSaveFileInfos 此对象为新上传对象
			List<String> fileKeys = new ArrayList<String>();
			for (FileInfo fileBean : toSaveFileInfos) {
				// 处理新上传
				fileKeys.add(fileBean.getFileKey());
			}
			fileList.addAll(toSaveFileInfos);
			editUploadFilesByKey(
					(String[]) fileKeys.toArray(new String[fileKeys.size()]),
					dbSession, 2);
			List<FileInfo> tempFileInfos2 = new ArrayList<FileInfo>();
			tempFileInfos2.addAll(savedFileInfos);
			savedFileInfos.removeAll(tempFileInfos1);
			// savedFileInfos删除的附件
			fileKeys.clear();
			for (FileInfo fileBean : savedFileInfos) {
				// 处理删除
				fileKeys.add(fileBean.getFileKey());
			}
			editUploadFilesByKey(
					(String[]) fileKeys.toArray(new String[fileKeys.size()]),
					dbSession, 0);
			tempFileInfos2.removeAll(savedFileInfos);
			fileList.addAll(tempFileInfos2);
		} else {// 新增
			List<String> fileKeys = new ArrayList<String>();
			for (FileInfo fileBean : toSaveFileInfos) {
				// 处理新上传
				fileKeys.add(fileBean.getFileKey());
			}
			fileList.addAll(toSaveFileInfos);
			editUploadFilesByKey(
					(String[]) fileKeys.toArray(new String[fileKeys.size()]),
					dbSession, 2);
		}
		// if((savedFileInfos.size() == toSaveFileInfos.size()) &&
		// savedFileInfos.containsAll(toSaveFileInfos)){
		// return savedPath;
		// }else{
		// List<FileInfo> tempFileInfos1 = new ArrayList<FileInfo>();
		// tempFileInfos1.addAll(toSaveFileInfos);
		// // 新上传的文件
		// toSaveFileInfos.removeAll(savedFileInfos);
		// //toSaveFileInfos 此对象为新上传对象
		// List<String> fileKeys = new ArrayList<String>();
		// for (FileInfo fileBean : toSaveFileInfos) {
		// //处理新上传
		// fileKeys.add(fileBean.getFileKey());
		// fileList.addAll(toSaveFileInfos);
		// }
		// editUploadFilesByKey((String[])fileKeys.toArray(new
		// String[fileKeys.size()]),dbSession, usedStatus);
		// if(!savedFileInfos.isEmpty()){
		// List<FileInfo> tempFileInfos2 = new ArrayList<FileInfo>();
		// tempFileInfos2.addAll(savedFileInfos);
		// savedFileInfos.removeAll(tempFileInfos1);
		// //savedFileInfos删除的附件
		// fileKeys.clear();
		// for (FileInfo fileBean : savedFileInfos) {
		// //处理删除
		// fileKeys.add(fileBean.getFileKey());
		// }
		// editUploadFilesByKey((String[])fileKeys.toArray(new
		// String[fileKeys.size()]), dbSession,usedStatus);
		// tempFileInfos2.removeAll(savedFileInfos);
		// fileList.addAll(tempFileInfos2);
		// }
		// }
		return JSON.toJSONString(fileList);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void editUploadFilesByKey(String[] fileKeys,
			MDBSession dbSession, int usedStatus) throws Exception {
		/** 修改附件表附件状态 begin **/
		StringBuilder updateSql = new StringBuilder(
				"update T_FILEUP_STORE set UsedStatus=? where FileKey=?");
		List updateParams = new ArrayList();
		updateParams.add(usedStatus);
		updateParams.add(fileKeys[0]);
		for (int i = 1; i < fileKeys.length; i++) {
			updateSql.append(" or FileKey=?");
			updateParams.add(fileKeys[i]);
		}
		// 修改使用状态
		dbSession.execNonQuery(updateSql.toString(), updateParams.toArray());
		/** 修改附件表附件状态 end **/
	}

	/**
    *
    * 创建Excel
    *
    * @param list 导出数据
    * @param headNames 表头
    * @param keyList 每列字段名
    * @param savePath 保存路径
    * @param sheetName 工作表名称
    *
    */
   public static void creatExcel(List<Map<Object, Object>> list, String[] headNames, List<String> keyList, String savePath,String sheetName,boolean noSerialNumber) {
       FileOutputStream os = null;
       try {
           os = new FileOutputStream(savePath);
           HSSFWorkbook wb = new HSSFWorkbook();
           HSSFSheet sheet = wb.createSheet(sheetName);
           for (int i = 0; i < keyList.size()+1; i++) {
        	   sheet.setColumnWidth(i, (sheetName.getBytes().length * 1 * 256));
           }
           HSSFCellStyle style = wb.createCellStyle();
           style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
           style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
           HSSFRow row = sheet.createRow(0);
           for (int i = 0; i < headNames.length; i++) {
               HSSFCell cell = row.createCell(i);
               cell.setCellStyle(style);
               cell.setCellType(HSSFCell.CELL_TYPE_STRING);
               cell.setCellValue(headNames[i]);
           }
           for (int i = 0; i < list.size(); i++) {
               HSSFRow dataRow = sheet.createRow(i + 1);

               for (int j = 0; j < keyList.size()+1; j++) {
            	   HSSFCell cell = dataRow.createCell(j);
                   cell.setCellStyle(style);
                   cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                   if(noSerialNumber){
                	   if(j < keyList.size()){
                		   cell.setCellValue(new HSSFRichTextString(list.get(i).get(keyList.get(j)) + ""));
                	   }
                   }else{
                	   if(j == 0){
                		   cell.setCellValue(new HSSFRichTextString((i + 1) + ""));
                	   }else{
                		   cell.setCellValue(new HSSFRichTextString(list.get(i).get(keyList.get(j-1)) + ""));
                	   }
                   }
               }
           }
           wb.write(os);
           os.flush();
           os.close();
       } catch (Exception e) {
    	   _log.error("创建Excel出错!", e);
    	   throw new RuntimeException(e);
       }
   }

	/**
	 * 封装文件属性
	 */
	static class FileInfo {
		private String fileKey;
		private String fileName;
		private String fileDate;
		private String previewUri;
		private long fileSize;

		public String getPreviewUri() {
			return previewUri;
		}

		public void setPreviewUri(String previewUri) {
			this.previewUri = previewUri;
		}

		public long getFileSize() {
			return fileSize;
		}

		public void setFileSize(long fileSize) {
			this.fileSize = fileSize;
		}

		/**
		 * 文件名称
		 *
		 * @return the fileName
		 */
		public String getFileName() {
			return fileName;
		}

		/**
		 * @param fileName
		 *            the fileName to set
		 */
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		/**
		 * 文件唯一标识Key，用于查找文件
		 *
		 * @return the fileKey
		 */
		public String getFileKey() {
			return fileKey;
		}

		/**
		 * @param fileKey
		 *            the fileKey to set
		 */
		public void setFileKey(String fileKey) {
			this.fileKey = fileKey;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fileKey == null) ? 0 : fileKey.hashCode());
			return result;
		}

		public String getFileDate() {
			return fileDate;
		}

		public void setFileDate(String fileDate) {
			this.fileDate = fileDate;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;

			FileInfo other = (FileInfo) obj;
			if (fileKey == null) {
				if (other.fileKey != null)
					return false;
			} else if (!fileKey.equals(other.fileKey))
				return false;
			return true;
		}
	}
}
