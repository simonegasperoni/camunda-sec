package com.orchestrator.sec.logic;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class SagalogAccess {

	public static Set<String> retrieveStates(String param1, String param2, String param3, String trace_id, String activity_id, String result) throws SQLException, ClassNotFoundException {

		Class.forName("org.postgresql.Driver");
		Connection c = DriverManager.getConnection(param1, param2,  param3);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery("select state from sagalog where trace_id = '"+trace_id+"' and activity_id = '"+activity_id+"' and result = '"+result+"'");
		HashSet<String> states = new HashSet<String>();

		while(rs.next()) {
			states.add(rs.getString("state"));
		}
		c.close();
		return states;
	}


	public static boolean transactionAborted(String param1, String param2, String param3, String trace_id) throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Connection c = DriverManager.getConnection(param1, param2,  param3);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery("select trace_id from sagalog where trace_id = '"+trace_id+"' and state ='Abort'and result = 'Waiting'");
		List<String> trace_ids=new ArrayList<String>();

		while(rs.next()) {
			trace_ids.add(rs.getString("trace_id"));
		}
		c.close();
		return trace_ids.size()>0;
	}

	public static void processAbort(String param1, String param2, String param3, String trace_id) throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Connection c = DriverManager.getConnection(param1, param2,  param3);
		Statement st = c.createStatement();
		st.executeUpdate("UPDATE public.sagalog SET result = 'Processed' WHERE trace_id = '"+trace_id+"' and state = 'Abort'");
	}

	public static int checkGroup(String param1, String param2, String param3, String trace_id, String activity_id) throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Connection c = DriverManager.getConnection(param1, param2,  param3);
		Statement st = c.createStatement();
		ResultSet rs = st.executeQuery("select count(distinct group_id) as gip from sagalog where trace_id = '"+trace_id+"' and activity_id ='"+activity_id+"'");
		rs.next();
		c.close();
		return rs.getInt("gip");

	}

	public static void writeRecord(String param1, String param2, String param3, String trace_id, String state, boolean compensation, String activity_id, String group, String result) throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Connection c = DriverManager.getConnection(param1, param2,  param3);
		PreparedStatement st = c.prepareStatement("INSERT INTO SAGALOG (trace_id, state, time_stamp, compensation, activity_id, group_id, result) VALUES (?, ?, ?, ?, ?, ?, ?)");
		st.setString(1, trace_id);
		st.setString(2, state);
		st.setTimestamp(3, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
		st.setBoolean(4, compensation);
		st.setString(5, activity_id);
		st.setString(6, group);
		st.setString(7, result);
		st.execute();
		st.close();
		c.close();
	}

}