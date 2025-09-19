package org.springblade.system.user.mybatis;

import com.vingsoft.crypto.CryptoFactory;
import com.vingsoft.crypto.CryptoType;
import com.vingsoft.crypto.ICrypto;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 自定义mybatis处理器，用于数据库字段脱敏加解密
 */

public class SM4EncryptHandler extends BaseTypeHandler {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		ICrypto crypto = CryptoFactory.createCrypto(CryptoType.SM4);
		if(!crypto.checkDataIsEncrypt((String)parameter)){
			String res = crypto.encrypt((String)parameter);
			ps.setString(i, res);
		} else {
			ps.setString(i, (String)parameter);
		}
	}
	@Override
	public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
		String columnValue = rs.getString(columnName);
		String res = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(columnValue);
		return res;
	}
	@Override
	public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		String columnValue = rs.getString(columnIndex);
		String res = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(columnValue);
		return res;
	}

	@Override
	public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		String columnValue = cs.getString(columnIndex);
		String res = CryptoFactory.createCrypto(CryptoType.SM4).decrypt(columnValue);
		return res;
	}
}
