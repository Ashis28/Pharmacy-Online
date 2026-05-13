import { MedicineService } from './medicine.service';
import { of } from 'rxjs';

const mockHttp = { get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn() } as any;

function makeService(): MedicineService {
  return new MedicineService(mockHttp);
}

describe('MedicineService', () => {

  beforeEach(() => jest.clearAllMocks());

  it('getAllMedicines() calls GET /api/catalog/medicines', () => {
    mockHttp.get.mockReturnValue(of([]));
    makeService().getAllMedicines().subscribe();
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('/api/catalog/medicines'));
  });

  it('getMedicineById() calls GET with correct id', () => {
    mockHttp.get.mockReturnValue(of({}));
    makeService().getMedicineById(7).subscribe();
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('/medicines/7'));
  });

  it('searchMedicines() calls GET with name param', () => {
    mockHttp.get.mockReturnValue(of([]));
    makeService().searchMedicines('aspirin').subscribe();
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('search?name=aspirin'));
  });

  it('addMedicine() calls POST /api/catalog/medicines', () => {
    mockHttp.post.mockReturnValue(of({}));
    const med = { name: 'Test', price: 10, stockQuantity: 5, requiresPrescription: false };
    makeService().addMedicine(med as any).subscribe();
    expect(mockHttp.post).toHaveBeenCalledWith(
      expect.stringContaining('/medicines'), med
    );
  });

  it('updateMedicine() calls PUT with id', () => {
    mockHttp.put.mockReturnValue(of({}));
    const med = { name: 'Updated', price: 20, stockQuantity: 3, requiresPrescription: false };
    makeService().updateMedicine(5, med as any).subscribe();
    expect(mockHttp.put).toHaveBeenCalledWith(expect.stringContaining('/medicines/5'), med);
  });

  it('deleteMedicine() calls DELETE with id', () => {
    mockHttp.delete.mockReturnValue(of(undefined));
    makeService().deleteMedicine(3).subscribe();
    expect(mockHttp.delete).toHaveBeenCalledWith(expect.stringContaining('/medicines/3'));
  });

  it('getLowStockCount() calls GET low-stock-count endpoint', () => {
    mockHttp.get.mockReturnValue(of(2));
    makeService().getLowStockCount().subscribe(count => expect(count).toBe(2));
    expect(mockHttp.get).toHaveBeenCalledWith(expect.stringContaining('low-stock-count'));
  });

  it('approvePrescription() calls PUT approve endpoint', () => {
    mockHttp.put.mockReturnValue(of({}));
    makeService().approvePrescription(10).subscribe();
    expect(mockHttp.put).toHaveBeenCalledWith(
      expect.stringContaining('/prescriptions/10/approve'), {}
    );
  });

  it('rejectPrescription() calls PUT reject endpoint with reason', () => {
    mockHttp.put.mockReturnValue(of({}));
    makeService().rejectPrescription(10, 'Illegible').subscribe();
    expect(mockHttp.put).toHaveBeenCalledWith(
      expect.stringContaining('/prescriptions/10/reject'),
      { reason: 'Illegible' }
    );
  });
});
