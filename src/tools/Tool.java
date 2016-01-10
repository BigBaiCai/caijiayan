package tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Tool {
	public static boolean isNetworkAvailable(Context context){
		//ConnectivityManager主要管理和网络连接相关的操作 ,记得注册权限
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);//获取系统的连接服务  
		if(connectivityManager==null){
			return false;//表示没有网络
		}else{
			//表示有网络
			/*//倘若手机目前不在网络的服务范围，则connec.getActiveNetworkInfo()会返回null
			 * 1.NetworkInfo info = connec.getActiveNetworkInfo(); 
				2.NetworkInfo []allinfo=  connec.getAllNetworkInfo(); 
				5种联网类型
				String typeName = info.getTypeName(); //cmwap/cmnet/wifi/uniwap/uninet  
				info.getTypeName();     // 以何种方式连线 [WIFI]
				info.getState();        // 连线状态 [CONNECTED]
				info.isAvailable();     // 网络是否可用 [true]
				info.isConnected();     // 网络是否已经连接 [true]
				info.isConnectedOrConnecting(); // 网络是否已经连接或者连接中 [true]
				info.isFailover();      // 网络是否有问题 [false]
				info.isRoaming();       // 网络是否在漫游中 [false]
			 */
			NetworkInfo[] info = connectivityManager.getAllNetworkInfo();//获取网络的连接情况  
			if(info!=null){
				//info表示集合 network表示集合里的元素，表示遍历。
				for(NetworkInfo network : info){
					//网络连接上了
					if(network.getState()==NetworkInfo.State.CONNECTED){
						return true;
					}
				}
			}
		}
		return false;
		
	}
}
