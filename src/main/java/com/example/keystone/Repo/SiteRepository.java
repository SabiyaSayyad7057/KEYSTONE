package com.example.keystone.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.keystone.Entity.Site;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

}