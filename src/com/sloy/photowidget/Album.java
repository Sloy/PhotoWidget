package com.sloy.photowidget;

class Album implements Comparable<Album>{

	public Album(String name){
		this(name,1);
	}
	public Album(String name, Integer count) {
		super();
		this.name = name;
		this.count = count;
	}

	public String name;
	public String directory;
	public Integer count;

	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(getClass() != obj.getClass()){
			return false;
		}
		Album other = (Album)obj;
		if(name == null){
			if(other.name != null){
				return false;
			}
		}else if(!name.equals(other.name)){
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Album another) {
		return this.name.compareTo(another.name);
	}
}