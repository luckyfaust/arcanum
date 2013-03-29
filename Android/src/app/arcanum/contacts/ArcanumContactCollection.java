package app.arcanum.contacts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import app.arcanum.contracts.PhoneNumber;

public class ArcanumContactCollection implements Serializable, Iterable<ArcanumContact> {
	private static final long serialVersionUID = 2763479958883524751L;
	private final HashMap<String, ArcanumContact> _contacts = new HashMap<String, ArcanumContact>();
	private final HashMap<String, HashSet<String>> _phoneCache = new HashMap<String, HashSet<String>>();
	
	public ArcanumContactCollection() { 
		super();
	}
	public ArcanumContactCollection(java.util.Map<String,ArcanumContact> map) {
		for(Entry<String, ArcanumContact> entry : map.entrySet()) {
			final ArcanumContact contact = entry.getValue();
			_contacts.put(entry.getKey(), contact);
			buildCacheFor(contact);
		}
	}
	public ArcanumContactCollection(java.util.Collection<ArcanumContact> values) { 
		for(ArcanumContact contact : values) {
			_contacts.put(contact.LookupKey, contact);
			buildCacheFor(contact);
		}
	}
	
	public boolean add(ArcanumContact contact) {
		if(_contacts.containsKey(contact.LookupKey))
			return false;
		
		buildCacheFor(contact);
		return _contacts.put(contact.LookupKey, contact) == null;
	}
	
	public ArcanumContact first() {
		if(_contacts.size() > 0) {
			for(Entry<String, ArcanumContact> entry : _contacts.entrySet())
				return entry.getValue();
		}
		return null;
	}
		
	public ArcanumContact findByKey(String lookupKey) {
		return _contacts.get(lookupKey);
	}
	
	public ArcanumContactCollection findByHash(String phoneHash) {
		ArcanumContactCollection result = new ArcanumContactCollection();
		HashSet<String> lookups = _phoneCache.get(phoneHash);
		for(String lookup : lookups) {
			result.add(_contacts.get(lookup));
		}
		return result;
	}
	
	public ArcanumContactCollection findByPhone(PhoneNumber phone) {
		return findByHash(phone.getHash());
	}
	
	private void buildCacheFor(final ArcanumContact contact) {
		for(PhoneNumber phone : contact.PhoneNumbers) {
			final String hash = phone.getHash();
			if(!_phoneCache.containsKey(hash))
				_phoneCache.put(hash, new HashSet<String>());
			_phoneCache.get(hash).add(contact.LookupKey);
		}
	}
	
	@Override
	public Iterator<ArcanumContact> iterator() {
		return _contacts.values().iterator();
	}
}
