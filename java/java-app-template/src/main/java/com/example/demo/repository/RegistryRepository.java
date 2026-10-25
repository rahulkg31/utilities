package com.example.demo.repository;

import java.util.List;

import com.example.demo.entity.RegistryEntity;

public interface RegistryRepository extends BaseRepository<RegistryEntity> {
	List<RegistryEntity> findByPrivateIp(String ip);
	int getMaxPortByPrivateIp(String ip);
}
