package x.retain.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetainvariablesRepository extends CrudRepository<Retainvariables, Integer> {

  Retainvariables findById(int id);

  Retainvariables findByDevicehandle(int handle);

  Retainvariables findByPropertyhandle(int handle);

  Retainvariables findByDevicehandleAndPropertyhandle(int devicehandle, int propertyhandle);
}
