package ravenscar;
public class LTMemory extends ScopedMemory
{
  public LTMemory(long size)
  {
    super(size);
  }
  
  public LTMemory(SizeEstimator size)
  {
    super(size);
  }
}
