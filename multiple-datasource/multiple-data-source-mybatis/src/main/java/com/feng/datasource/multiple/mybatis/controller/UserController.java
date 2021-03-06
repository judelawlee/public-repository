package com.feng.datasource.multiple.mybatis.controller;

import com.feng.datasource.multiple.mybatis.entity.User;
import com.feng.datasource.multiple.mybatis.repository.cluster.ClusterUserMapper;
import com.feng.datasource.multiple.mybatis.repository.master.MasterUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制层
 * @日期： 2018年7月5日 下午11:18:04
 * @作者： Chendb
 */
@RestController
public class UserController {

	@Autowired
	private MasterUserMapper masterUserMapper;

	@Autowired
	private ClusterUserMapper clusterUserMapper;

	@GetMapping("/all")
	public Map<String, Object> all() {
		Map<String, Object> data = new HashMap<>();
		data.put("master", masterUserMapper.getAll());
		data.put("cluster", clusterUserMapper.getAll());
		return data;
	}

	/************************主库控制层接口-start******************************/
	@RequestMapping("user/getAllMaster")
	public List<User> getAllMaster() {
		return masterUserMapper.getAll();
	}

	@RequestMapping("user/getMaster")
	public User getUserMaster(Integer userId) {
		return masterUserMapper.get(userId);
	}

	@RequestMapping("user/insertMaster")
	public Integer insertMaster(User userModel) {
		masterUserMapper.insert(userModel);
		// 必须使用对象获取id，否则无法获取到主键，而是获取到0（插入失败）或者1（插入成功）
		return userModel.getUserId();
	}

	@RequestMapping("user/updateMaster")
	public Integer updateMaster(User userModel) {
		return masterUserMapper.update(userModel);
	}

	@RequestMapping("user/deleteMaster")
	public Integer deleteMaster(Integer userId) {
		return masterUserMapper.delete(userId);
	}
	/************************主库控制层接口-end******************************/

	/************************从库控制层接口-start******************************/
	@RequestMapping("user/getAllCluster")
	public List<User> getAllCluster() {
		return clusterUserMapper.getAll();
	}

	@RequestMapping("user/getCluster")
	public User getUserCluster(Integer userId) {
		return clusterUserMapper.get(userId);
	}

	@RequestMapping("user/insertCluster")
	public Integer insertCluster(User userModel) {
		clusterUserMapper.insert(userModel);
		// 必须使用对象获取id，否则无法获取到主键，而是获取到0（插入失败）或者1（插入成功）
		return userModel.getUserId();
	}

	@RequestMapping("user/updateCluster")
	public Integer updateCluster(User userModel) {
		return clusterUserMapper.update(userModel);
	}

	@RequestMapping("user/deleteCluster")
	public Integer deleteCluster(Integer userId) {
		return clusterUserMapper.delete(userId);
	}
	/************************从库控制层接口-end******************************/
}
