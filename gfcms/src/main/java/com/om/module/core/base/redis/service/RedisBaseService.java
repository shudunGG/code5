package com.om.module.core.base.redis.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service("redisBaseService")
public class RedisBaseService {

	@Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 执行set操作
     * @param key
     * @param value
     */
    public void set(final String key,final String value){
        stringRedisTemplate.opsForValue().set(key,value);
    }

    /**
     * 执行get操作
     * @param key
     * @return
     */
    public String get(final String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 执行删除操作
     * @param key
     * @return
     */
    public boolean delete(final String key){
        return stringRedisTemplate.delete(key) ;
    }

    /**
     * 执行set操作并设置过期时间,单位秒
     * @param key
     * @param value
     * @param seconds
     */
    public void set(final String key,final String value,final Integer seconds){
        //TimeUnit.MINUTES 分,TimeUnit.SECONDS秒
        stringRedisTemplate.opsForValue().set(key,value,seconds, TimeUnit.SECONDS);
    }

    /**
     * 执行hst操作
     * @param key
     * @param mapkey
     * @param mapvalue
     */
    public void hset(final String key,final String mapkey,final String mapvalue){
        stringRedisTemplate.opsForHash().put(key, mapkey, mapvalue);
    }

    /**
     * 执行Hash getAll操作,返回一个map
     * @param key
     * @return
     */
    public Map<String,String> hashGetAll(final String key){
        return (Map)stringRedisTemplate.opsForHash().entries(key);
    }

    /**
     * 执行Hash del操作
     * @param key
     * @param strings
     * @return
     */
    public long hashDel(final String key,final String[] strings){
        return stringRedisTemplate.opsForHash().delete(key,strings);
    }

    /**
     * 得到hash 的key操作
     * @param key
     * @return
     */
    public Set<String> hashKeys(final String key){
        return (Set) stringRedisTemplate.opsForHash().keys(key);
    }

    /**
     * 执行hvalues操作
     * @param key
     * @return
     */
    public List<String> hashValues(final String key){
        return (List)stringRedisTemplate.opsForHash().values(key);
    }

    /**
     * 执行hash的get操作
     * @param key
     * @param mapkey
     * @return
     */
    public String hashGet(final String key,final String mapkey){
        return (String)stringRedisTemplate.opsForHash().get(key,mapkey);
    }

    /**
     * 将一个Map集合存放到hash里面
     * @param key
     * @param mapValue
     */
    public void hashMapSet(final String key,final Map<String,String> mapValue){
        stringRedisTemplate.opsForHash().putAll(key,mapValue);
    }

    /**
     * 执行队列的 lpush操作
     * @param key
     * @param value
     * @return
     */
    public long lPush(final String key,final String value){
        return stringRedisTemplate.opsForList().leftPush(key,value);
    }

    /**
     * 执行队列的 left pop操作
     * @param key
     * @return
     */
    public String lPop(final String key){
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    /**
     * 执行 队列的 right pop 操作
     * @param key
     * @return
     */
    public String rPop(final String key){
        return stringRedisTemplate.opsForList().rightPop(key);
    }

    /**
     * 执行list插入操作,在列表的尾部添加一个值,返回列表的长度
     * @param key
     * @param value
     * @return
     */
    public Long rPush(final String key,final String value){
        return stringRedisTemplate.opsForList().rightPush(key,value);
    }

    /**
     * 在列表list的尾部添加多个值,返回列表的长度
     * @param key
     * @param value
     * @return
     */
    public Long rPush(final String key,final String[] value){
        return stringRedisTemplate.opsForList().rightPushAll(key,value);
    }

    /**
     * 获取列表list指定范围的值
     * @param key
     * @param start
     * @param end
     * @return
     */
    public List<String> listRang(final String key,final long start,final long end){
        return stringRedisTemplate.opsForList().range(key,start,end);
    }

    /**
     * 通过索引获取list列表中的元素
     * @param key
     * @param index
     * @return
     */
    public String listIndex(final String key,final long index){
        return stringRedisTemplate.opsForList().index(key,index);
    }

    /**
     * 获取列表list的长度
     * @param key
     * @return
     */
    public Long listLen(final String key){
        return stringRedisTemplate.opsForList().size(key);
    }

    /**
     * 设置元素的生效时间
     * @param key
     * @param seconds
     * @return
     */
    public boolean expire(final String key,final Integer seconds){
        return stringRedisTemplate.expire(key,seconds, TimeUnit.SECONDS);
    }
}
