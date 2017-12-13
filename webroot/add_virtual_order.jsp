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
    
    if(subjectId != null && subjectId != "" && num != null && regionId != null && userId != null) {

        try {
            String driverName = "com.mysql.jdbc.Driver";
            String connUrl = "jdbc:mysql://rm-p0w1d3fb180472liw.mysql.australia.rds.aliyuncs.com:3306/tbdb?user=tb2&password=Znz-2000"; 
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
                    <option value="1000146">蘑菇</option>
                    <option value="1000145">草莓</option>
                    <option value="1000143">学伟</option>
                    <option value="1000132">学鹏</option>
                    <option value="1000152">琳姐</option>
                    <option value="1000163">李迪</option>
                    <option value="1000165">萌麻</option>
                    <option value="1000156">小四</option>
                    <option value="1000158">黄成</option>
                    <option value="1000159">在睡觉</option>
                    <option value="1000160">Richard</option>
                    <option value="1000162">May</option>
                    <option value="1000164">Brain</option>
                    <option value="1000167">Maggie</option>
                    <option value="1000168">冰魔</option>
                    <option value="1000169">Icy</option>
                    <option value="1000170">镜子世界</option>
                    <option value="1000171">黄氏</option>
                    <option value="1000172">呆萌</option>
                    <option value="1000173">XM</option>
                    <option value="1000176">Rachel</option>
                    <option value="1000175">小心</option>
                    <option value="1000396">Chris_Huang</option>
                    <option value="1000179">曼女神</option>
                    <option value="1000525">Bonny</option>
                    <option value="1000545">Rachel</option>
                    <option value="1000539">Ying.H</option>
                    <option value="1000176">Rachel真是不容易啊...</option>
                    <option value="1000393">曼蒂陶</option>
                    <option value="1000647">川</option>
                    <option value="1000648">翼</option>
                    <option value="1000646">李强</option>
                    <option value="1000649">Jesse</option>
                    <option value="1000651">node</option>
                    <option value="1000650">一杯浓茶</option>
                    <option value="1000652">tara</option>
                    <option value="1000656">一妹妈</option>
                    <option value="1000657">超超</option>
                    <option value="1000658">姣儿</option>
                    <option value="1000444">ivanessa</option>
                </select>
                <br />
            </p>
            <input type="submit" value="提交" />
            <input type="reset" value="重置" />
        </form> 
    </body> 
</html>
