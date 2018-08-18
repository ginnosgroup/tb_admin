<%@ page language="java" import="java.util.*,java.sql.*" pageEncoding="utf-8"%>
<%@ page contentType="text/html;charset=utf-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>添加虚拟订单</title>
        <META http-equiv=Content-Type content="text/html; charset=utf-8">
    </head>
    <body>
<%  
    request.setCharacterEncoding("UTF-8");  
    response.setCharacterEncoding("UTF-8");  
    response.setContentType("text/html;charset=utf-8");  
    
    String subjectId = request.getParameter("subject_id");
    String num = request.getParameter("num");
    String regionId = request.getParameter("region_id");
    String userId = request.getParameter("user_id");

    String driverName = "com.mysql.jdbc.Driver";
    // String connUrl = "jdbc:mysql://localhost:3306/tbdb?user=root&password=hello";
    String connUrl = "jdbc:mysql://rm-p0w1d3fb180472liw.mysql.australia.rds.aliyuncs.com:3306/tbdb?user=tb2&password=Znz-2000";
    
    if(subjectId != null && subjectId != "" && num != null && regionId != null && userId != null) {
        try {
            Class.forName(driverName).newInstance();
            Connection conn = DriverManager.getConnection(connUrl);
            Statement stmt = conn.createStatement();
            stmt.executeQuery("SET NAMES UTF8");
            String insert_sql = "INSERT INTO tb_order (gmt_create, gmt_modify, name, state, subject_id, num, amount, pay_type, pay_code, pay_amount, pay_date, create_price, finish_price, user_id, introducer_user_id, adviser_id, adviser_date, region_id) VALUES (now(), now(), '虚拟订单', 'WAIT', " + subjectId + ", " + num + ", '0.00', 'BALANCE', 'NULL', '0.00', now(), '0.00', null, " + userId + ", null, 1000000, now(), " + regionId + ");";        
            // out.println("SQL: " + insert_sql);
            try {
                stmt.execute(insert_sql);
            }catch(Exception e) {
                e.printStackTrace();
            }
            conn.close();
        }catch (Exception e) { 
            e.printStackTrace(); 
        }
        out.println("<font color='red'>执行完成.</font>");
    } else {
        out.println("请输入虚拟订单参数......");
    }

%>
        <form action="" method="post">
            <p>
                编号: <input type="text" name="subject_id" value="" size="6" maxlength="7" required="required" /></br>
                数量: <input type="number" name="num" value="1" min="1" max="99" /></br>
                区域:
                <select name="region_id"> 
                    <option value="1000011">Sydney</option>
                    <option value="1000012">Brisbane</option>
                    <option value="1000013">Melbourne</option>
                    <option value="1000014">Adelaide</option>
                    <option value="1000021">Canberra</option>
                    <option value="1000022">Hobart</option>
                </select>
                <br />
                顾客:
                <select name="user_id">
<%
    try {
        Class.forName(driverName).newInstance();
        Connection conn = DriverManager.getConnection(connUrl);
        Statement stmt = conn.createStatement();
        stmt.executeQuery("SET NAMES UTF8");
        String select_sql = "SELECT * FROM tb_user WHERE auth_type = 'V'";
        try {
            ResultSet rs = stmt.executeQuery(select_sql);
            while (rs.next()) {
%>
                    <option value='<%=rs.getString("id")%>'><%=rs.getString("name")%></option>
<%
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        conn.close();
    }catch (Exception e) {
        e.printStackTrace();
    }
%>
                </select>
                <br />
            </p>
            <input type="submit" value="提交" />
            <input type="reset" value="重置" />
        </form> 
    </body> 
</html>
